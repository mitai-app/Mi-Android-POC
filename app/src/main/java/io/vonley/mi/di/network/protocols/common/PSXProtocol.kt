package io.vonley.mi.di.network.protocols.common

import io.vonley.mi.base.BaseClient
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.models.enums.Feature
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

interface PSXNotifier {
    suspend fun notify(message: String)
    suspend fun boot(ps3boot: Boot)
    suspend fun buzzer(buzz: Buzzer)
}

interface PSXProtocol : BaseClient {

    override val http: OkHttpClient get() = service.http
    override fun post(url: String, body: RequestBody, headers: Headers) =
        service.post(url, body, headers)

    override fun post(url: String, body: RequestBody, headers: Headers, response: Callback) =
        service.post(url, body, headers, response)

    override fun getRequest(url: String, response: Callback) = service.getRequest(url, response)
    override fun getRequest(url: String) = service.getRequest(url)


    val service: PSXService
    val feature: Feature
    val socket: Socket

    suspend fun sendAndRecv(data: String?): String? {
        return try {
            send(data)
            recv()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun send(data: String?) {
        try {
            val pw = PrintWriter(socket.getOutputStream())
            pw.println(data)
            pw.flush()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    suspend fun recv(): String? {
        return try {
            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            br.readLine()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun recvAll(): ByteArray {
        return try {
            socket.getInputStream().readBytes()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ByteArray(0)
        }
    }

    @Throws(IOException::class)
    fun close() {
        if (socket.isConnected) {
            socket.close()
        }
    }

}