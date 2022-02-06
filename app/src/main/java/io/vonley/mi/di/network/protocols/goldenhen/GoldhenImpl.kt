package io.vonley.mi.di.network.protocols.goldenhen

import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.extensions.*
import io.vonley.mi.models.Payload
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.Socket
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GoldhenImpl @Inject constructor(
    override val service: PSXService,
    val server: MiServer
) : Goldhen {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override suspend fun sendPayloads(
        callback: GoldhenCallback,
        vararg payloads: Payload
    ): Map<String, Payload> {
        val map = service.createSocket(service.target, Feature.GOLDENHEN)?.let { _ ->
            // we know that socket is initialized now
            val pairs = payloads.map { payload ->
                delay(3000)
                val pair: Pair<String, Payload>
                val fail: suspend (msg: String) -> Pair<String, Payload> = { msg ->
                    payload.status = -1
                    msg.e(TAG)
                    withContext(Dispatchers.Main) {
                        callback.onPayloadFailed(payload)
                    }
                    Pair(payload.name, payload)
                }
                val success: suspend (msg: String) -> Pair<String, Payload> = { msg ->
                    msg.e(TAG)
                    payload.status = 1
                    withContext(Dispatchers.Main) {
                        callback.onSent(payload)
                    }
                    Pair(payload.name, payload)
                }
                val writing: suspend (msg: String) -> Unit = { msg ->
                    msg.e(TAG)
                    payload.status = 0
                    withContext(Dispatchers.Main) {
                        callback.onWriting(payload)
                    }
                }
                pair = when {
                    payload.name.endsWith(".bin") -> {
                        try {
                            suspend fun send(socket: Socket): Pair<String, Payload> {
                                socket.getOutputStream().use { out ->
                                    writing("writing payload")
                                    out.write(payload.data)
                                    out.flush()
                                }
                                socket.close()
                                "Payload '${payload.name}' Sent!".i(TAG)
                                return success("success")
                            }
                            if (!socket.isConnected || socket.isOutputShutdown || socket.isClosed || socket.isInputShutdown) {
                                val createSocket = service.createSocket(service.target, Feature.GOLDENHEN)
                                if (createSocket == null) {
                                    fail("something went wrong")
                                } else {
                                    send(createSocket)
                                }
                            } else {
                                send(socket)
                            }
                        } catch (e: Throwable) {
                            fail("Failed to send payload '${payload.name}': ${e.message}")
                        }
                    }
                    payload.name.endsWith(".pkg") -> {
                        try {
                            if (service.target?.features?.contains(Feature.RPI) == false) {
                                return@map fail("target does not have golden hen")
                            }
                            "Target has Remote Package Installer".i(TAG)
                            val urls = server.hostPackage(payload)
                            val toJson =
                                PSXServiceImpl.RPI(PSXServiceImpl.RPI.Type.direct, urls).toJson()
                            val contentType = "application/json".toMediaType()
                            val requestBody = toJson.toRequestBody(contentType)
                            "json: $toJson".d(TAG)
                            val res = post(
                                "http://${service.target?.ip}:12800/api/install",
                                requestBody,
                                Headers.headersOf()
                            )
                            val body = res?.body ?: run {
                                return@map fail("No response")
                            }
                            writing("writing payload")
                            val map = body.string().fromJson<HashMap<String, String>>() ?: run {
                                return@map fail("Unable to parse response")
                            }
                            val status = map["status"] ?: run {
                                return@map fail("status not found?")
                            }
                            if (status == "fail") {
                                return@map fail(map["error"] ?: "Failed to send payload")
                            } else {
                                return@map success("Payload sent")
                            }
                        } catch (e: Throwable) {
                            return@map fail(e.message ?: "Something went wrong")
                        }
                    }
                    else -> fail("something isnt right")
                }
                return@map pair
            }
            return hashMapOf<String, Payload>(*pairs.toTypedArray())
        } ?: hashMapOf<String, Payload>(*payloads.onEach { it.status = -1 }
            .map { Pair(it.name, it) }.toTypedArray())
        return map
    }
}
