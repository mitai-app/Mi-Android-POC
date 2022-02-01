package io.vonley.mi.di.network

import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.text.format.Formatter
import io.vonley.mi.di.network.handlers.ClientHandler
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.models.Client
import io.vonley.mi.di.network.impl.SyncServiceImpl
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.CoroutineScope
import java.net.Socket

/**
 * Implementations are in this class
 * @see SyncServiceImpl
 */
interface SyncService : CoroutineScope {

    val target: Client?
    val wifiInfo: WifiInfo
    val activeNetworkInfo: NetworkInfo?
    val activeNetwork: Network?
    val localDeviceIpInt: Int get() = wifiInfo.ipAddress
    val localDeviceIp: String get() = Formatter.formatIpAddress(localDeviceIpInt)
    val isConnected: Boolean
    val handlers: HashMap<Class<*>, ClientHandler>
    val TAG: String

    fun cleanup()
    fun initialize()
    fun isNetworkAvailable(): Boolean
    fun isWifiAvailable(): Boolean
    fun getClients(loop: Boolean = false)
    fun setTarget(client: Client)
    fun addConsoleListener(console: OnConsoleListener)
    fun stop()
    fun removeConsoleListener(console: OnConsoleListener)
    fun createSocket(client: Client, feature: Feature): Socket?
    fun getSocket(client: Client, feature: Feature): Socket?
}
