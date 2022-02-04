package io.vonley.mi.di.network

import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import io.vonley.mi.base.BaseClient
import io.vonley.mi.di.network.handlers.ClientHandler
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.models.Client
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.Socket
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext

interface PSXService : BaseClient, SyncService {

    interface PSXListener {
        fun onFinished()
        fun onWriting(payload: PayloadAdapter.Payload)
        fun onSent(payload: PayloadAdapter.Payload)
        fun onPayloadFailed(payload: PayloadAdapter.Payload)
    }

    val features: LiveData<List<Feature>>

    val sync: SyncService

    val manager: SharedPreferenceManager

    val targetIp get() = target?.ip

    /**
     * Uploads file to web
     */
    fun uploadBin(file: ByteArray, callback: Callback) {
        if (target == null) return
        val url = "$targetIp:${manager.featurePort.ports.first()}"
        val body: RequestBody = file.toRequestBody()
        Log.e("URL", url)
        post(url, body, Headers.headersOf(), callback)
    }

    /**
     * Uploads file to web
     */
    fun uploadBin(file: File, callback: Callback) {
        if (target == null) return
        val url = "$targetIp:${manager.featurePort.ports.first()}"
        val body: RequestBody = file.asRequestBody()
        Log.e("URL", url)
        post(url, body, Headers.headersOf(), callback)
    }

    fun uploadBin (server: MiServer, payloads: ArrayList<PayloadAdapter.Payload>, callback: PSXListener)

    override fun cleanup() = sync.cleanup()

    override fun initialize() = sync.initialize()

    override fun isNetworkAvailable() = sync.isNetworkAvailable()

    override fun isWifiAvailable() = sync.isWifiAvailable()

    override fun getClients(loop: Boolean) = sync.getClients(loop)

    override fun setTarget(client: Client) = sync.setTarget(client)

    override fun addConsoleListener(console: OnConsoleListener) = sync.addConsoleListener(console)

    override fun stop() = sync.stop()

    override fun removeConsoleListener(console: OnConsoleListener) = sync.removeConsoleListener(console)

    override fun createSocket(client: Client, feature: Feature): Socket? = sync.createSocket(client, feature)
    override fun getSocket(client: Client, feature: Feature): Socket? = sync.getSocket(client, feature)

    override val target: Client?
        get() = sync.target

    val job: Job

    override val TAG: String
        get() = PSXService::class.java.name

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    override val wifiInfo: WifiInfo
        get() = sync.wifiInfo
    override val activeNetworkInfo: NetworkInfo?
        get() = sync.activeNetworkInfo
    override val activeNetwork: Network?
        get() = sync.activeNetwork
    override val isConnected: Boolean
        get() = sync.isConnected
    override val handlers: HashMap<Class<*>, ClientHandler>
        get() = sync.handlers
}
