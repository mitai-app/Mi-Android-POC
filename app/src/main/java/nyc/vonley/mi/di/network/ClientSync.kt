package nyc.vonley.mi.di.network

import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.text.format.Formatter
import kotlinx.coroutines.CoroutineScope
import nyc.vonley.mi.di.network.handlers.ClientHandler

interface ClientSync: CoroutineScope {

    val activeNetworkInfo: NetworkInfo?

    val activeNetwork: Network?

    val connectionInfo: WifiInfo

    val ipAddr: Int
        get() = connectionInfo.ipAddress

    val ip: String
        get() = Formatter.formatIpAddress(ipAddr)

    val handlers: HashMap<Class<*>, ClientHandler>
    val TAG: String

    fun cleanup()
    fun initialize()
    val isConnected: Boolean
    fun isNetworkAvailable(): Boolean
    fun isWifiAvailable(): Boolean
}