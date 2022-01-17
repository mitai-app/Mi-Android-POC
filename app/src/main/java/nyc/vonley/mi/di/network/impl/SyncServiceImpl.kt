package nyc.vonley.mi.di.network.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import kotlinx.coroutines.*
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.di.network.handlers.ClientHandler
import nyc.vonley.mi.di.network.handlers.impl.ConsoleClientHandler
import nyc.vonley.mi.di.network.listeners.OnConsoleListener
import nyc.vonley.mi.extensions.client
import nyc.vonley.mi.extensions.console
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.persistence.AppDatabase
import java.lang.ref.WeakReference
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
    database: AppDatabase
) : SyncService, CoroutineScope {

    override val TAG = SyncServiceImpl::class.java.name
    override val handlers: HashMap<Class<*>, ClientHandler> = hashMapOf()
    private val mContextRef: WeakReference<Context> = WeakReference<Context>(context)
    private lateinit var cm: ConnectivityManager
    private lateinit var wm: WifiManager
    private val job = Job()
    private lateinit var activeJob: Job


    override val isConnected: Boolean get() = isWifiAvailable()

    //region Override
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    var _target: Client? = null

    override val target: Client?
        get() = _target

    override val activeNetworkInfo: NetworkInfo?
        get() = cm.activeNetworkInfo

    override val activeNetwork: Network?
        get() = cm.activeNetwork

    override val wifiInfo: WifiInfo
        get() = wm.connectionInfo

    override val ipAddressInt: Int
        get() = wifiInfo.ipAddress

    override val ipAddress: String
        get() = Formatter.formatIpAddress(ipAddressInt)
    //endregion

    init {
        handlers[ConsoleClientHandler::class.java] = ConsoleClientHandler(database.consoleDao())
        val con = mContextRef.get()
        if (con != null) {
            cm = con.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            wm = con.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
    }

    override fun isNetworkAvailable(): Boolean {
        val actNw = cm.getNetworkCapabilities(activeNetwork ?: return false) ?: return false
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
        val actNw = cm.getNetworkCapabilities(activeNetwork ?: return false) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    }

    override fun initialize() = Unit

    /**
     * Gets Active Clients & Gets Consoles
     */
    override fun getClients() {
        val block: suspend CoroutineScope.() -> Unit = {
            fetchConsolesListAsync(fetchClientListAsync())
        }
        if (!this::activeJob.isInitialized) activeJob = launch(block = block)
        if (this.activeJob.isActive) return
        if (this.activeJob.isCompleted) {
            if (!activeJob.start()) {
                activeJob = launch(block = block)
            }
        }
    }

    override fun setTarget(client: Client) {
        this._target = client
    }


    override fun addConsoleListener(console: OnConsoleListener) {
        this[ConsoleClientHandler::class.java].listeners[console.javaClass] = console
    }

    /**
     * TODO: Clean up all listeners so there no
     * memory leak1
     */
    override fun cleanup() = Unit

    /**
     * Fetch PS4 & PS3 Consoles on the current
     * network for jailbroken devices
     */
    private fun fetchConsoles(clients: List<Client>): List<Console> {
        try {
            Log.i(TAG, "[FetchConsoles::Start] Active Network: $activeNetworkInfo")
            Log.i(TAG, "[Device Local IP] $ipAddress")
            val prefix = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1)
            Log.i(TAG, "[Local IP Prefix] $prefix")
            val consoles = clients
                .mapNotNull { client -> client.console() }
                .filter { client -> client.features.isNotEmpty() }
            launch {
                withContext(Dispatchers.Main) {
                    this@SyncServiceImpl[ConsoleClientHandler::class.java].handle(consoles)
                }
            }
            Log.v(TAG, "[FetchConsoles::End] End of Scan #: ${consoles.size}")
            return consoles
        } catch (t: Throwable) {
            Log.e(TAG, "[FetchConsoles::End] Well... that's not good. ${t.message}", t)
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
            Log.i(TAG, "[FetchClients::Start] Active Network: $activeNetworkInfo")
            Log.i(TAG, "[Device Local IP] $ipAddress")
            val prefix = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1)
            Log.i(TAG, "[Local IP Prefix] $prefix")
            val clients = ArrayList<Client>()
            for (i in 1 until 256) {
                try {
                    val ip = "$prefix$i"
                    val byName = activeNetwork?.getByName(ip) ?: continue
                    if (byName.isReachable(100)) {
                        Log.i(
                            TAG,
                            "[Client IP] (${byName.hostAddress ?: byName.canonicalHostName}) is reachable"
                        )
                        val client = byName.client(wi = wifiInfo)
                        client.lastKnownReachable = true
                        clients.add(client)
                    } else {
                        Log.e(
                            TAG,
                            "[Client IP] ${byName.hostAddress ?: byName.canonicalHostName} is unreachable"
                        )
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "[Error] ${e.message}")
                }
            }
            Log.v(TAG, "[FetchClients::End] End of scan, #: ${clients.size}")
            return clients
        } catch (t: Throwable) {
            Log.e(TAG, "[Error] Hmmm. that's not good. ${t.message}", t)
        }
        return emptyList()
    }

    private suspend fun fetchClientListAsync(): List<Client> {
        return coroutineScope { async { return@async fetchClients() }.await() }
    }

    private suspend fun fetchConsolesListAsync(clients: List<Client>) {
        return coroutineScope { async { return@async fetchConsoles(clients) }.await() }
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