package io.vonley.mi.di.network.protocols.goldenhen

import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.extensions.*
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GoldenHenImpl @Inject constructor(
    override val service: PSXService,
    val server: MiServer
) : GoldenHen {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    suspend fun uploadBin(
        vararg payloads: PayloadAdapter.Payload
    ) {
        payloads.onEach { payload ->
            when {
                payload.name.endsWith(".bin") -> {
                    try {
                        socket.getOutputStream().also { out ->
                            out.write(payload.data)
                            out.flush()
                        }
                        "Payload '${payload.name}' Sent!".i(TAG)
                    } catch (e: Throwable) {
                        "Failed to send payload '${payload.name}': ${e.message}".e(TAG, e)
                    }
                }
                payload.name.endsWith(".pkg") -> {
                    val fail: (msg: String) -> Unit = { msg ->
                        payload.status = -1
                    }
                    val success: (msg: String) -> Unit = { msg ->
                        payload.status = 1
                    }
                    try {
                        if (service.target?.features?.contains(Feature.RPI) == false) {
                            return@onEach
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
                            fail("No response")
                            return@onEach
                        }
                        val map = body.string().fromJson<HashMap<String, String>>() ?: run {
                            fail("Unable to parse response")
                            return@onEach
                        }
                        val status = map["status"] ?: run {
                            fail("status not found?")
                            return@onEach
                        }
                        if (status == "fail") {
                            fail(map["error"] ?: "Failed to send payload")
                        } else {
                            success("Payload sent")
                        }
                    } catch (e: Throwable) {
                        fail(e.message?:"Something went wrong")
                    }
                }
            }
            delay(3000)
        }
    }
}
