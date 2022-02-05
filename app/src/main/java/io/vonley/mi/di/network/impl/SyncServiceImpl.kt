package io.vonley.mi.di.network.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.BuildConfig
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.network.handlers.ClientHandler
import io.vonley.mi.di.network.handlers.base.BaseClientHandler
import io.vonley.mi.di.network.handlers.impl.ConsoleClientHandler
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.extensions.*
import io.vonley.mi.models.Client
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.persistence.AppDatabase
import io.vonley.mi.persistence.ConsoleDao
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext


/**
 * ////////////////////////////////////////
 * ///   Author: Mr-Smithy-x (Vonley)   ///
 * ///     Purpose: Fetch Consoles      ///
 * ///        Project Mi: 1.2.22		///
 * ////////////////////////////////////////
 *
 * Sync Service is a class that meant to scan
 * the network for connect clients. whether
 * console or not, only to then to be further
 * analyzed and converted to be stored in
 * ConsoleDao Sqlite
 *
 * It is injected as a singleton and runs 1
 * sync job at a time.
 */
class SyncServiceImpl constructor(
    context: Context,
    database: AppDatabase,
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient
) : SyncService, CoroutineScope {

    override val TAG = SyncServiceImpl::class.java.name
    override val handlers: HashMap<Class<*>, ClientHandler> = hashMapOf()
    private val mContextRef: WeakReference<Context> = WeakReference<Context>(context)
    private lateinit var cm: ConnectivityManager
    private lateinit var wm: WifiManager
    private val job = Job()
    private val dao: ConsoleDao = database.consoleDao()

    private lateinit var activeJob: Job

    override val isConnected: Boolean get() = isWifiAvailable()

    //region Override
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private val cachedTargets = hashMapOf<String, EnumMap<Feature, Socket>>()


    override fun getSocket(client: Client?, feature: Feature): Socket? {
        if (client == null) return null
        return cachedTargets[client.ip]?.get(feature)
    }

    override fun createSocket(client: Client?, feature: Feature): Socket? {
        if (client == null) return null
        if (!cachedTargets.containsKey(client.ip)) {
            cachedTargets[client.ip] = EnumMap<Feature, Socket>(Feature::class.java)
        }
        if (!cachedTargets[client.ip]!!.containsKey(feature)) {
            for (port in feature.ports) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(client.ip, port), 2000)
                    if (socket.isConnected) {
                        cachedTargets[client.ip]!![feature] = socket
                        break;
                    }
                } catch (con: ConnectException) {
                    "${con.message}".e(TAG)
                } catch (e: Throwable) {
                    "${e.message}".e(TAG)
                }
            }
        }
        var socket = cachedTargets[client.ip]?.get(feature)
        if (socket != null && socket.isClosed) {
            try {
                val lastPort: Int = socket.port
                socket = Socket()
                socket.connect(InetSocketAddress(client.ip, lastPort))
                cachedTargets[client.ip]!![feature] = socket
            } catch (e: Throwable) {
                cachedTargets[client.ip]!![feature] = null
            }
        }
        return cachedTargets[client.ip]!![feature]
    }

    private val _liveTarget: MutableLiveData<Client> = MutableLiveData()

    override val liveTarget: LiveData<Client>
        get() = _liveTarget


    override val activeNetworkInfo: NetworkInfo?
        get() = cm.activeNetworkInfo

    @get:RequiresApi(Build.VERSION_CODES.M)
    override val activeNetwork: Network?
        get() = cm.activeNetwork

    override val wifiInfo: WifiInfo
        get() = wm.connectionInfo

    override val localDeviceIpInt: Int
        get() = wifiInfo.ipAddress

    override val localDeviceIp: String
        get() = Formatter.formatIpAddress(localDeviceIpInt)
    //endregion

    init {
        handlers[ConsoleClientHandler::class.java] = ConsoleClientHandler(database.consoleDao())
        val con = mContextRef.get()
        if (con != null) {
            cm = con.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            wm = con.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
    }

    override fun isNetworkAvailable(): Boolean {
        val actNw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.getNetworkCapabilities(activeNetwork ?: return false) ?: return false
        } else {
            return activeNetworkInfo?.isConnected == true
        }
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    override fun isWifiAvailable(): Boolean {
        val actNw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.getNetworkCapabilities(activeNetwork ?: return false) ?: return false
        } else {
            return activeNetworkInfo?.isConnected == true
        }
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    }

    override fun initialize() = Unit

    /**
     * Gets Active Clients & Gets Consoles
     */
    override fun getClients(loop: Boolean) {
        val block: suspend CoroutineScope.() -> Unit = {
            do {
                /**
                 * First fetch and see if console are legit
                 */
                dao.get(wifiInfo.ssid).value?.let { consoles ->
                    fetchConsolesListAsync(consoles)
                }
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "[Finding Clients]")
                }
                val clients = fetchClientListAsync()
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "[Finding Consoles]")
                }
                fetchConsolesListAsync(clients)

                if (BuildConfig.DEBUG) {
                    if (loop) {
                        Log.e(TAG, "[fetching again in ${manager.scanInterval} seconds]")
                    } else {
                        Log.e(TAG, "[ran only once]")
                    }
                }
                val ms = (manager.scanInterval * 1000L) // can every 1500
                delay(ms)
            } while (loop)
        }
        if (!this::activeJob.isInitialized) activeJob = launch(block = block)
        if (this.activeJob.isActive) return
        if (this.activeJob.isCompleted) {
            if (!activeJob.start()) {
                activeJob = launch(block = block)
            }
        }
    }


    override fun stop() {
        if (this::activeJob.isInitialized) {
            if (this.activeJob.isActive) {
                this.activeJob.cancel()
            }
        }
    }

    override fun setTarget(client: Client) {
        manager.targetName = client.ip
        synchronized(_liveTarget) {
            _liveTarget.postValue(client)
        }
    }


    override fun addConsoleListener(console: OnConsoleListener) {
        this[ConsoleClientHandler::class.java].listeners[console.javaClass] = console
    }

    override fun removeConsoleListener(console: OnConsoleListener) {
        this[ConsoleClientHandler::class.java].listeners.remove(console.javaClass)
    }

    /**
     * TODO: Clean up all listeners so there no
     * memory leak
     */
    override fun cleanup() {
        this.handlers.onEach {
            val handler = it.value
            if (handler is BaseClientHandler<*, *>) {
                handler.listeners.clear()
            }
        }
        cachedTargets.onEach { entry ->
            val ip = entry.key
            entry.value.onEach { entryFeature ->
                val feature = entryFeature.key
                val socket = entryFeature.value
                if (socket.isConnected) {
                    socket.close()
                }
            }
        }.clear()
    }

    /**
     * Fetch PS4 & PS3 Consoles on the current
     * network for jailbroken devices
     */
    private suspend fun fetchConsoles(clients: List<Client>): List<Console> {
        try {
            val consoles = clients
                .mapNotNull { client ->
                    val console = client.console(this)
                    if (console == null) {
                        dao.delete(client.ip, client.wifi)
                    }
                    console
                }.filter { console -> console.features.isNotEmpty() }
            withContext(Dispatchers.Main) {
                this@SyncServiceImpl[ConsoleClientHandler::class.java].handle(consoles)
            }
            "[FetchConsoles::End] End of Scan #: ${consoles.size}".v(TAG)
            return consoles
        } catch (t: Throwable) {
             "[FetchConsoles::End] Well... that's not good. ${t.message}".e(TAG, t)
        }
        return emptyList()
    }

    /**
     * Fetch All Connected Clients on the network
     * for potential match (IPV4 Clients Only)
     * TODO: Accommodate for IPV6 Clients
     */
    private fun fetchClients(): List<Client> {
        try {
            "[FetchClients::Start] Active Network: $activeNetworkInfo".i(TAG)
            val prefix = localDeviceIp.substring(0, localDeviceIp.lastIndexOf(".") + 1)
            val clients = ArrayList<Client>()
            for (i in 1 until 256) {
                try {
                    val ip = "$prefix$i"
                    val byName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activeNetwork?.getByName(ip) ?: continue
                    } else {
                        InetAddress.getByName(ip) ?: continue
                    }

                    if (byName.isReachable(100)) {
                        "[Client IP] (${byName.hostAddress ?: byName.canonicalHostName}) is reachable".v(
                            TAG
                        )
                        val client = byName.client(wi = wifiInfo)
                        client.lastKnownReachable = true
                        clients.add(client)
                    } else {
                        "[Client IP] ${byName.hostAddress ?: byName.canonicalHostName} is unreachable".v(
                            TAG
                        )
                    }
                } catch (e: Throwable) {
                    "[Error] ${e.message}".e(TAG)
                }
            }
            "[FetchClients::End] End of scan, #: ${clients.size}".v(TAG)
            return clients
        } catch (t: Throwable) {
            Log.e(TAG, "[Error] Hmmm. that's not good. ${t.message}", t)
        }
        return emptyList()
    }

    private suspend fun fetchClientListAsync(): List<Client> {
        val coroutineScope = coroutineScope {
            val async = async {
                return@async fetchClients()
            }
            async.await()
        }
        return coroutineScope
    }

    private suspend fun fetchConsolesListAsync(clients: List<Client>) {
        return coroutineScope {
            val async = async {
                return@async fetchConsoles(clients)
            }
            async.await()
        }
    }


    companion object {
        const val TAG = "client"
    }

}


operator fun <T : ClientHandler> SyncService.set(clazz: Class<T>, data: T) {
    handlers[clazz] = data
}

operator fun <T : ClientHandler> SyncService.get(clazz: Class<T>): T {
    return handlers[clazz] as T
}

operator fun <T : Client> SyncService.set(client: T?, feature: Feature) {
    createSocket(client, feature)
}

operator fun <T : Client> SyncService.get(client: T?, feature: Feature): Socket? {
    return getSocket(client, feature)
}
