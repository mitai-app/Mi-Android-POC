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
                for (i in 1 until 256) {
                    val testIp = "$prefix$i";
                    val client = InetAddress.getByName(testIp).client()
                    if (client.reachable) {
                        clients[client.hostName] = client
                        val activePorts = client.activePorts
                        reachables.add(client)
                        Log.e(TAG, "Host: (${client.hostName} : ${client.address.hostAddress}) ${client.address.hostName} is reachable! w/ $activePorts");
                    } else {
                        clients[client.hostName] = client
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Well that's not good.", t);
            Log.e(TAG, t.message ?: "Somethign wrong")
        }
        return reachables
    }

    private suspend fun fetchClientListAsync(): List<Client> {
        return coroutineScope {
            async<List<Client>> {
                return@async fetchClients()
            }.await()
        }
    }


    fun getClients(callable: (clients: List<Client>) -> Unit) {
        launch {
            val clients = fetchClientListAsync()
            withContext(Dispatchers.Main) {
                callable(clients)
            }
        }
    }


}
