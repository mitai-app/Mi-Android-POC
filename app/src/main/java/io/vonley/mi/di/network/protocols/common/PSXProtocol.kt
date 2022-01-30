package io.vonley.mi.di.network.protocols.common

import io.vonley.mi.base.BaseClient
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.models.enums.Feature
import okhttp3.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

interface PSXNotify {
    fun notify(message: String)
}

interface PSXSystem {
    fun boot(ps3boot: Boot)
    fun refresh()
    fun insert()
    fun eject()
    fun unmount()
}

interface PSXProtocol : PSXNotify, PSXSystem, BaseClient {

    override val http: OkHttpClient get() = service.http
    override fun post(url: String, body: RequestBody, headers: Headers) = service.post(url, body, headers)
    override fun post(url: String, body: RequestBody, headers: Headers, response: Callback) = service.post(url, body, headers, response)
    override fun getRequest(url: String, response: Callback) = service.getRequest(url, response)
    override fun getRequest(url: String) = service.getRequest(url)


    val service: PSXService
    val feature: Feature
    val socket: Socket

    fun send(data: String?): String? {
        return try {
            val pw = PrintWriter(socket.getOutputStream())
            pw.println(data)
            pw.flush()
            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            br.readLine()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun quickSend(data: String?) {
        try {
            val pw = PrintWriter(socket.getOutputStream())
            pw.println(data)
            pw.flush()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun quickRead(): String? {
        return try {
            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            br.readLine()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    fun close() {
        if (socket.isConnected) {
            socket.close()
        }
    }

    override fun refresh() {
        service.target?.let {  target ->
            if (Feature.WEBMAN in target.features) {
                val url = ("http://${target.ip}:80/refresh.ps3")
            }
        }
    }

    override fun insert() {
        service.target?.let {  target ->
            if (Feature.WEBMAN in target.features) {
                val url = ("http://${target.ip}:80/insert.ps3")
            }
        }
    }

    override fun eject() {
        service.target?.let {  target ->
            if (Feature.WEBMAN in target.features) {
                val url = ("http://${target.ip}:80/eject.ps3")
            }
        }
    }

    override fun unmount() {
        service.target?.let {  target ->
            if (Feature.WEBMAN in target.features) {
                val url = ("http://${target.ip}:80/mount.ps3/unmount")
            }
        }
    }
}