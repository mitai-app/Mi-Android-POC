package nyc.vonley.mi.di.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import kotlinx.coroutines.*
import nyc.vonley.mi.extensions.client
import nyc.vonley.mi.extensions.console
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import java.lang.ref.WeakReference
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext


//TODO: Consider using a view model
class ClientSync constructor(context: Context) : CoroutineScope {

    private val mContextRef: WeakReference<Context> = WeakReference<Context>(context);

    private val clients: HashMap<String, Client> = hashMapOf()
    private val consoles: HashMap<String, Console> = hashMapOf()

    val activeClients: List<Client> get() = clients.values.filter { client -> client.lastKnownReachable }
    val activeConsoles: List<Console> get() =  consoles.values.toList()
    lateinit var cm: ConnectivityManager
    lateinit var wm: WifiManager

    val activeNetwork: NetworkInfo?
        get() = cm.activeNetworkInfo

    val connectionInfo: WifiInfo
        get() = wm.connectionInfo

    val ipAddress: Int
        get() = connectionInfo.ipAddress

    val ipString: String
        get() = Formatter.formatIpAddress(ipAddress)

    protected val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        const val TAG = "client"
    }

    init {
        val con = mContextRef.get()
        if (con != null) {
            cm = con.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            wm = con.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
    }

    ////////////////////////////////////////
    ///   Author: Mr-Smithy-x (Vonley)   ///
    ///     Purpose: Fetch Consoles      ///
    ///        Project Mi: 1.2.22		 ///
    ////////////////////////////////////////


    /**
     * Fetch PS4 & PS3 Consoles on the current
     * network for jailbroken devices
     */
    fun fetchConsoles(clients: List<Client>): List<Console> {
        try {
            Log.i(TAG, "[FetchConsoles::Start] Active Network: $activeNetwork")
            Log.i(TAG, "[Device Local IP] $ipString")
            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
            Log.i(TAG, "[Local IP Prefix] $prefix")
            val consoles = clients
                .filter { client -> client.getActivePorts().isNotEmpty() }
                .mapNotNull { client -> client.console() }
            consoles.forEachIndexed { index, console ->
                this.consoles[console.ip] = console
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
     * TODO: Accomodate for IPV6 Clients
     */
    fun fetchClients(): List<Client> {
        val reachables: ArrayList<Client> = ArrayList()
        try {
            Log.i(TAG, "[FetchClients::Start] Active Network: $activeNetwork")
            Log.i(TAG, "[Device Local IP] $ipString")
            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
            Log.i(TAG, "[Local IP Prefix] $prefix")
            for (i in 1 until 256) {
                try {
                    val ip = "$prefix$i"
                    val client = InetAddress.getByName(ip).client()
                    if (client.reachable) {
                        Log.i(TAG, "[Client IP] (${client.hostName}) is reachable")
                        clients[client.hostName] = client
                        reachables.add(client)
                    } else {
                        Log.e(TAG, "[Client IP] ${client.hostName} is unreachable")
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "[Error] ${e.message}")
                }
            }
            Log.v(TAG, "[FetchClients::End] End of scan, #: ${reachables.size}")
            return reachables
        } catch (t: Throwable) {
            Log.e(TAG, "[Error] Hmmm. that's not good. ${t.message}", t)
        }
        return emptyList()
    }

    private suspend fun fetchClientListAsync(): List<Client> {
        return coroutineScope { async { return@async fetchClients() }.await() }
    }

    private suspend fun fetchConsolesListAsync(clients: List<Client>): List<Console> {
        return coroutineScope { async { return@async fetchConsoles(clients) }.await() }
    }

    lateinit var activeJob: Job

    /**
     * Gets Active Clients & Gets Consoles
     */
    fun getClients(callableClients: (clients: List<Client>) -> Unit, callableConsoles: (consoles: List<Console>) -> Unit) {
        val block: suspend CoroutineScope.() -> Unit = {
            val clients = fetchClientListAsync()
            withContext(Dispatchers.Main) {
                Log.e(TAG, "$clients")
                callableClients(clients)
            }

            val consoles = fetchConsolesListAsync(clients)
            withContext(Dispatchers.Main){
                Log.e(TAG, "$consoles")
                callableConsoles(consoles)
            }
        }
        if (!this::activeJob.isInitialized) activeJob = launch(block = block)
        if (this.activeJob.isActive) return
        if (this.activeJob.isCompleted) {
            if (!activeJob.start()) {
                activeJob = launch(block = block)
            }
        }
    }

}
