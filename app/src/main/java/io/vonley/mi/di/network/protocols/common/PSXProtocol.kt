package io.vonley.mi.di.network.protocols

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIResponse
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIResponse.Code
import io.vonley.mi.models.enums.Feature
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

interface PSXFunction {
    fun notify(message: String)
}

interface PSXProtocol : PSXFunction {

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
}