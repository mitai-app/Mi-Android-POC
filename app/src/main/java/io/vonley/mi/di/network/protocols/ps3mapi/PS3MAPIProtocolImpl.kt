package io.vonley.mi.di.network.protocols.ps3mapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import io.vonley.mi.di.network.protocols.common.models.Process
import io.vonley.mi.di.network.protocols.common.models.Temperature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.internal.notifyAll
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

class PS3MAPIProtocolImpl(
    override val service: PSXService,
) : PS3MAPIProtocol, PS3MAPIProtocol.JMAPIListener {

    var server: ServerSocket? = null
    var data_sock: Socket? = null

    override val listener: PS3MAPIProtocol.JMAPIListener = this
    private var _liveProcesses = MutableLiveData<List<Process>>()
    override val liveProcesses: LiveData<List<Process>> get() = _liveProcesses

    private var _processes: ArrayList<Process> = arrayListOf()
        set(value) {
            if (value.isNotEmpty()) {
                synchronized(_processes) {
                    _liveProcesses.postValue(value)
                    _liveProcesses.notifyAll()
                }
                field = value
            }
        }
    override val processes: List<Process> get() = _processes
    override var attached: Boolean = false
    override var process: Process? = null
    override val pids: List<Process>
        get() {
            val process: ArrayList<Process> = ArrayList<Process>()
            val text = "PROCESS GETALLPID"
            val (_, response, code) = PS3MAPIResponse.parse(super.send(text) ?: return emptyList())
            for (s in response.split("\\|".toRegex()).toTypedArray()) {
                if (s == "0") continue
                val (_, response1, _) = PS3MAPIResponse.parse(
                    super.send("PROCESS GETNAME $s") ?: continue
                )
                process.add(Process.create(response1, s))
            }
            if (process.size > 0) {
                this._processes = process
                listener.onJMAPIPS3Process(PS3MAPIResponse.Code.REQUESTSUCCESSFUL, process)
            }
            return process
        }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun openDataSocket() {
        try {
            send("PASV")?.let { response ->
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


    override fun onJMAPIError(error: String?) {

    }

    override fun onJMAPIResponse(
        ps3Op: PS3MAPIProtocol.PS3OP?,
        responseCode: PS3MAPIResponse.Code?,
        message: String?
    ) {

    }

    override fun onJMAPIPS3Process(responseCode: PS3MAPIResponse.Code?, processes: List<Process>?) {

    }

    override fun onJMAPITemperature(
        responseCode: PS3MAPIResponse.Code?,
        temperature: Temperature?
    ) {

    }

}