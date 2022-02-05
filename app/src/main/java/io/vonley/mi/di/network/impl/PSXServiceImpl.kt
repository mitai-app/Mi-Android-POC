package io.vonley.mi.di.network.impl

import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.extensions.*
import io.vonley.mi.models.Client
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {

    companion object {
        suspend fun notify(
            sync: SyncService,
            client: Client,
            msg: String,
            feature: Feature
        ): Boolean {
            val socket = sync[client, feature]

            if (socket?.isConnected == true) {

            }
            return false
        }
    }

    /**
     * Initialize socket for designated feature. Newly created sockets are stored in
     * @see SyncServiceImpl.cachedTargets\. If socket is already stored and if
     * the connection have not close then the sock will not be created.
     * features will notify any observers who have an open socket connection
     * thats listening in on target change
     * @see Client.openActivePorts - This also takes place happens here.
     */
    override fun initialize() {
        target?.let { target ->
            val ip = target.ip
            val allowed = target.type.features
            val filter = target.features.filter { it in allowed }
            launch {
                val feats = filter.map { feature -> this@PSXServiceImpl[target] = feature; feature }
                    .filter { this@PSXServiceImpl[target, it] != null }
                withContext(Dispatchers.Main) {
                    synchronized(liveTarget) {
                        setTarget(target)
                    }
                }
            }
        }
    }

    override val TAG = PSXService::class.java.name

    suspend fun notify(msg: String): Boolean {
        target?.let { console ->
            val common = console.features.firstOrNull {
                it in arrayOf(
                    Feature.PS3MAPI,
                    Feature.WEBMAN,
                    Feature.CCAPI
                )
            } ?: return false

            return notify(this, console, msg, common)
        }
        return false
    }

    data class RPI(val type: RPI.Type, val packages: Array<String>) {
        enum class Type {
            direct,
            ref_pkg_url
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is RPI) return false

            if (type != other.type) return false
            if (!packages.contentEquals(other.packages)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + packages.contentHashCode()
            return result
        }
    }

    override fun uploadBin(
        server: MiServer,
        payloads: ArrayList<PayloadAdapter.Payload>,
        callback: PSXService.PSXListener
    ) {
        launch {
            val bins = payloads.filter { it.name.endsWith(".bin") }
            val pkgs = payloads.filter { it.name.endsWith(".pkg") }
            if (bins.isNotEmpty()) {
                bins.onEach { payload ->
                    try {
                        val socket = Socket()
                        socket.connect(
                            InetSocketAddress(
                                targetIp,
                                manager.featurePort.ports.first()
                            ),
                            3000
                        )
                        withContext(Dispatchers.Main) {
                            callback.onWriting(payload)
                        }
                        socket.getOutputStream().use { out ->
                            out.write(payload.data)
                            out.flush()
                        }
                        withContext(Dispatchers.Main) {
                            payload.status = 1
                            callback.onSent(payload)
                        }
                        "Payload '${payload.name}' Sent!".i(TAG)
                        delay(3000)
                    } catch (e: Throwable) {
                        "Failed to send payload '${payload.name}': ${e.message}".e(TAG, e)
                        withContext(Dispatchers.Main) {
                            payload.status = -1
                            callback.onPayloadFailed(payload)
                        }
                    }
                }
            }

            if (pkgs.isNotEmpty() && target?.features?.contains(Feature.RPI) == true) {
                "Target has Remote Package Installer".i(TAG)
                pkgs.onEach { payload ->
                    try {
                        val urls = server.hostPackage(payload)
                        val toJson = RPI(RPI.Type.direct, urls).toJson()
                        val contentType = "application/json".toMediaType()
                        val body = toJson.toRequestBody(contentType)
                        "json: $toJson".d(TAG)
                        withContext(Dispatchers.Main) {
                            callback.onWriting(payload)
                        }
                        val res =
                            post(
                                "http://$targetIp:12800/api/install",
                                body,
                                Headers.headersOf()
                            )
                        withContext(Dispatchers.Main) {
                            res?.body?.let { body ->
                                body.string()
                                    .fromJson<HashMap<String, String>>()?.let { map ->
                                        map["status"]?.let { status ->
                                            if (status == "fail") {
                                                payload.status = -1
                                                callback.onPayloadFailed(payload)
                                            } else {
                                                payload.status = 1
                                                callback.onSent(payload)
                                            }
                                        } ?: run {
                                            payload.status = -1
                                            callback.onPayloadFailed(payload)
                                        }
                                    } ?: run {
                                    payload.status = -1
                                    callback.onPayloadFailed(payload)
                                }
                            } ?: run {
                                "Response was empty...".d(TAG)
                                payload.status = -1
                                callback.onPayloadFailed(payload)
                            }
                        }

                    } catch (e: Throwable) {
                        "Something went wrong: ${e.message}".e(TAG, e)
                        withContext(Dispatchers.Main) {
                            payload.status = -1
                            callback.onPayloadFailed(payload)
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                callback.onFinished()
            }
        }
    }

    override val job: Job = Job()

}