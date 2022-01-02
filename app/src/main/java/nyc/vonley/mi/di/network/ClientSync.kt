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

    private val clients: HashMap<String, Client> = hashMapOf()
    private val consoles: HashMap<String, Console> = hashMapOf()

    val activeClients: List<Client> = clients.values.filter { client -> client.reachable }
    val activeConsoles: List<Console> = consoles.values.toList()

    protected val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        const val TAG = "client"
    }

    private val mContextRef: WeakReference<Context> = WeakReference<Context>(context);

    fun fetchClients(): ArrayList<Client> {
        val reachables: ArrayList<Client> = ArrayList()
        try {
            val context = mContextRef.get();

            if (context != null) {

                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                val connectionInfo = wm.connectionInfo as WifiInfo
                val ipAddress = connectionInfo.ipAddress;
                val ipString = Formatter.formatIpAddress(ipAddress);

                Log.d(TAG, "activeNetwork: $activeNetwork");
                Log.d(TAG, "ipString: $ipString");

                val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: $prefix");
                for (i in 0 until 256) {
                    val testIp = "$prefix$i"
                    Log.d(TAG, "testIP: $testIp");

                    val client = InetAddress.getByName(testIp).client()
                    if (client.reachable) {
                        clients[client.hostName] = client
                        Log.e(TAG, "Host: (${client.hostName} : ${client.address.hostAddress}) is reachables");
                        val activePorts = client.activePorts
                        reachables.add(client)
                        if (activePorts.isNotEmpty()) {
                            Log.e(TAG, "Host: (${client.hostName} w/ $activePorts");
                            val console = client.console()
                            if (console != null) {
                                consoles[console.ip] = console
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Well that's not good.", t);
            Log.e(TAG, t.message ?: "Something wrong")
        }
        Log.e(TAG, "END OF SCAN")
        return reachables
    }

    private suspend fun fetchClientListAsync(): List<Client> {
        return coroutineScope {
            async<List<Client>> {
                return@async fetchClients()
            }.await()
        }
    }


    /**
     * Gets Active Clients & Gets Consoles
     */
    fun getClients(callable: (clients: List<Client>, consoles: List<Console>) -> Unit) {
        launch {
            val clients = fetchClientListAsync()
            withContext(Dispatchers.Main) {
                Log.e(TAG, "${consoles.values.toList()}")
                callable(clients, consoles.values.toList())
            }
        }
    }

}
