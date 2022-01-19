package nyc.vonley.mi.di.network

import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.text.format.Formatter
import kotlinx.coroutines.CoroutineScope
import nyc.vonley.mi.di.network.handlers.ClientHandler
import nyc.vonley.mi.di.network.listeners.OnConsoleListener
import nyc.vonley.mi.models.Client

interface SyncService: CoroutineScope {

    val target: Client?
    val wifiInfo: WifiInfo
    val activeNetworkInfo: NetworkInfo?
    val activeNetwork: Network?
    val ipAddressInt: Int get() = wifiInfo.ipAddress
    val ipAddress: String get() = Formatter.formatIpAddress(ipAddressInt)
    val isConnected: Boolean
    val handlers: HashMap<Class<*>, ClientHandler>
    val TAG: String

    fun cleanup()
    fun initialize()
    fun isNetworkAvailable(): Boolean
    fun isWifiAvailable(): Boolean
    fun getClients(loop: Boolean = false, delaySeconds: Int = 15)
    fun setTarget(client: Client)
    fun addConsoleListener(console: OnConsoleListener)
    fun stop()
    fun removeConsoleListener(console: OnConsoleListener)
}
