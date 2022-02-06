package io.vonley.mi.di.network.protocols.ps3mapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.models.Process
import io.vonley.mi.di.network.protocols.common.models.Temperature
import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

class PS3MAPIImpl(
    override val service: PSXService,
) : PS3MAPI, PS3MAPI.Listener {

    var server: ServerSocket? = null
    var data_sock: Socket? = null

    override var authed: Boolean = false
    override val listener: PS3MAPI.Listener = this
    private var _liveProcesses = MutableLiveData<List<Process>>()
    override val liveProcesses: LiveData<List<Process>> get() = _liveProcesses

    private var _br: BufferedReader? = null


    override suspend fun send(data: String?) {
        try {
            val pw = PrintWriter(socket.getOutputStream())
            "sending: ${data.toString()}".equals(TAG)
            pw.println(data)
            pw.flush()
        } catch (ex: Exception) {
            ex.printStackTrace()
            _br = null
        }
    }

    override suspend fun recv(): String? {
        return try {
            if (_br == null) {
                _br = socket.getInputStream().bufferedReader()
            }
            val readLine = _br?.readLine()
            readLine
        } catch (ex: Exception) {
            ex.printStackTrace()
            _br = null
            null
        }
    }

    private var _processes: ArrayList<Process> = arrayListOf()
        set(value) {
            if (value.isNotEmpty()) {
                synchronized(_processes) {
                    _liveProcesses.postValue(value)
                }
                field = value
            }
        }


    override val processes: List<Process> get() = _processes
    override var attached: Boolean = false
    override var process: Process? = null
    override suspend fun getPids(): List<Process> {
        val process: ArrayList<Process> = ArrayList<Process>()
        val text = "PROCESS GETALLPID"
        val (_, response, code) = PS3MAPIResponse.parse(sendAndRecv(text) ?: return emptyList())
        for (s in response.split("\\|".toRegex()).toTypedArray()) {
            if (s == "0") continue
            val (_, response1, _) = PS3MAPIResponse.parse(
                sendAndRecv("PROCESS GETNAME $s") ?: continue
            )
            process.add(Process.create(response1, s))
        }
        if (process.size > 0) {
            this._processes = process
            listener.onProcessReceived(PS3MAPIResponse.Code.REQUESTSUCCESSFUL, process)
        }
        return process
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override suspend fun openDataSocket() {
        try {
            sendAndRecv("PASV")?.let { response ->
                val pav = PS3MAPIResponse.parse(response)
                val start: Int = pav.response.indexOf("(") + 1
                val end: Int = pav.response.indexOf(")")
                val split: Array<String> =
                    pav.response.substring(start, end).split(",").toTypedArray()
                val ip = String.format(
                    "%s.%s.%s.%s",
                    split[0], split[1], split[2], split[3]
                )
                val port = (Integer.valueOf(split[4]) shl 8) + Integer.valueOf(
                    split[5]
                )
                data_sock = Socket()
                data_sock?.connect(InetSocketAddress(ip, port))
            } ?: run {

            }
        } catch (e: IOException) {
            throw e
        } catch (ex: Exception) {
            //throw JMAPIException("Malformed PASV")
        }
    }

    override fun connectDataSocket() {
        if (data_sock != null) // already connected (always so if passive mode)
            return
        try {
            data_sock = server?.accept() // Accept is blocking
            server?.close()
            server = null
            if (data_sock == null) {
                throw Exception("Could not establish server")
            }
        } catch (ex: Exception) {
            throw Exception("Failed to connect for data transfer: " + ex.message)
        }
    }

    override fun closeDataSocket(): Boolean {
        try {
            data_sock?.close()
            data_sock = null
            server = null
            return true
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return false
    }


    override fun onError(error: String?) {

    }

    override fun onResponse(
        ps3Op: PS3MAPI.PS3OP?,
        responseCode: PS3MAPIResponse.Code?,
        message: String?
    ) {

    }

    override fun onProcessReceived(responseCode: PS3MAPIResponse.Code?, processes: List<Process>?) {

    }

    override fun onTemperatureReceived(
        responseCode: PS3MAPIResponse.Code?,
        temperature: Temperature?
    ) {

    }

}