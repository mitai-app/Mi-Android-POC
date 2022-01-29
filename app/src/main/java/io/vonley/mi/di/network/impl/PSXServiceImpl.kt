package io.vonley.mi.di.network.impl

import android.util.Log
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.extensions.toJson
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {

    override val TAG = PSXService::class.java.name

    data class RPI(val type: RPI.Type, val packages: Array<String>) {
        enum class Type {
            direct,
            ref_pkg_url
        }
    }

    override fun uploadBin(
        server: MiServer,
        payloads: ArrayList<PayloadAdapter.Payload>,
        callback: PSXService.PSXListener
    ) {
        launch {
            payloads.onEach { payload ->
                when {
                    payload.name.endsWith(".bin") -> {

                        try {
                            val socket = Socket()
                            socket.connect(InetSocketAddress(targetIp, manager.featurePort.ports.first()))
                            withContext(Dispatchers.Main) {
                                callback.onWriting(payload)
                            }
                            socket.getOutputStream().use { out ->
                                out.write(payload.data)
                                out.flush()
                            }
                            socket.closeQuietly()
                            withContext(Dispatchers.Main) {
                                payload.status = 1
                                callback.onSent(payload)
                            }
                        } catch (e: Throwable) {
                            withContext(Dispatchers.Main) {
                                payload.status = -1
                                callback.onPayloadFailed(payload)
                            }
                        }
                    }
                    payload.name.endsWith(".pkg") -> {
                        try {
                            if (target?.features?.contains(Feature.RPI) == true) {
                                Log.e(TAG, "Has RPI")
                            }
                            val urls = server.hostPackage(payload)
                            val toJson = RPI(RPI.Type.direct, urls).toJson()
                            val contentType = "application/json".toMediaType()
                            val body = toJson.toRequestBody(contentType)
                            Log.e(TAG, "json: $toJson")
                            val res =
                                post("http://$targetIp:12800/api/install", body, Headers.headersOf())
                            Log.e(TAG,"Code: ${res.code}")
                            res.body?.let { body ->
                                Log.e(TAG, body.string())
                                withContext(Dispatchers.Main) {
                                    callback.onWriting(payload)
                                }
                            } ?: run {
                                Log.e(TAG, "IDK")
                                withContext(Dispatchers.Main) {
                                    payload.status = -1
                                    callback.onPayloadFailed(payload)
                                }
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "${e.message}")
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                payload.status = -1
                                callback.onPayloadFailed(payload)
                            }
                        }
                    }
                }
                delay(3000)
            }
            withContext(Dispatchers.Main) {
                callback.onFinished()
            }
        }
    }

    override val job: Job = Job()

}