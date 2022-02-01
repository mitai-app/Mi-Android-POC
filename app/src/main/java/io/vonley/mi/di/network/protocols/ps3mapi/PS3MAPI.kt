package io.vonley.mi.di.network.protocols.ps3mapi

import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.*
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.common.cmds.LedColor
import io.vonley.mi.di.network.protocols.common.cmds.LedStatus
import io.vonley.mi.di.network.protocols.common.models.*
import io.vonley.mi.di.network.protocols.ps3mapi.cmds.*
import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import io.vonley.mi.models.enums.Feature
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

interface PS3MAPI : PSXProtocol {

    override val feature: Feature get() = Feature.PS3MAPI
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    val listener: Listener
    val processes: List<Process>
    val liveProcesses: LiveData<List<Process>>
    var attached: Boolean
    var process: Process?
    var authed: Boolean


    override fun sendAndRecv(data: String?): String? {
        return if (authed && socket.isConnected) {
            super.sendAndRecv(data)
        } else null // Not Connected!
    }

    suspend fun connect(): Boolean {
        return when {
            authed && socket.isConnected -> true
            !authed && socket.isConnected -> {
                try {
                    val first: PS3MAPIResponse = PS3MAPIResponse.parse(recv() ?: return false)
                    if (first.success && first.code === PS3MAPIResponse.Code.PS3MAPICONNECTED) {
                        val second: PS3MAPIResponse = PS3MAPIResponse.parse(recv() ?: return false)
                        if (second.success && second.code === PS3MAPIResponse.Code.PS3MAPICONNECTEDOK) {
                            authed = true
                            return true
                        }
                        return true
                    }
                    true
                } catch (e: IOException) {
                    e.printStackTrace()
                    false
                }
            }
            else -> try {
                val sockAddr = InetSocketAddress(service.targetIp, feature.ports.first())
                socket.connect(sockAddr, 2000)
                connect()
            } catch (e: Throwable) {
                false
            }
        }
    }

    @Throws(IOException::class) // JMAPIException::class)
    fun openDataSocket()

    @Throws(Exception::class)
    fun connectDataSocket()
    fun closeDataSocket(): Boolean

    interface Listener {

        fun onJMAPIError(error: String?)
        fun onJMAPIResponse(ps3Op: PS3OP?, responseCode: PS3MAPIResponse.Code?, message: String?)
        fun onJMAPIPS3Process(
            responseCode: PS3MAPIResponse.Code?,
            processes: List<Process>?
        )

        fun onJMAPITemperature(responseCode: PS3MAPIResponse.Code?, temperature: Temperature?)
    }

    interface MemoryListener {
        @Throws(Exception::class)
        fun onGetMemory(
            process: String?,
            offset: Long,
            size: Int,
            signedBytes: ByteArray?,
            unsignedBytes: IntArray?
        )

        fun onSetMemory(response: String?, offset: Long)
        fun onMemoryError(response: String?, process: String?, offset: Long)
    }

    /**
     * Attaches to the ps3 process with the process id provided by developer
     *
     * @param process Attaches to the process
     * @return whether the process attachment was successful
     * @see PS3Process
     */
    suspend fun attach(process: Process?): Boolean {
        if (containsProcess(pids, process)) {
            this.process = process
            attached = true
        }
        return false
    }


    /**
     * Attaches to the ps3 process automatically.
     *
     * @return whether the process attachment was successful
     * @see
     */
    suspend fun smartAttach(): Boolean {
        this.process = smartContainsProcess(pids)
        attached = process != null
        return attached
    }

    suspend fun smartContainsProcess(processes: List<Process>): Process? =
        processes.find { p -> p.name.lowercase().contains("eboot") }

    suspend fun containsProcess(processes: List<Process>, process: Process?): Boolean {
        for (p in processes) {
            if (p == process) return true
        }
        return false
    }

    /**
     * Detaches from the process
     *
     * @see
     */
    suspend fun detach() {
        process = null
        attached = false
    }

    enum class VERSION {
        CORE, SERVER
    }

    /**
     * Gets the firmware of your ps3
     * @return PS3 Firmware Version ie. 4.75
     * @see
     */
    val fwVersion: String?
        get() {
            val s = "PS3 GETFWVERSION"
            val response = sendAndRecv(s) ?: return null
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(response)
            val str =
                StringBuilder(Integer.toHexString(Integer.valueOf(res.response))).insert(1, ".")
                    .toString()
            listener.onJMAPIResponse(PS3OP.FWVERSION, res.code, str)
            return res.response
        }

    /**
     * Disables SYSCall mode
     *
     * @see com.mrsmyx.JMAPI.SYSCALL8MODE
     */
    suspend fun disableSysCall(mode: PS3MAPIResponse.SYSCALL8MODE) {
        val s = "PS3 DISABLESYSCALL " + mode.ordinal.toString()
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return)
        res.response
        listener.onJMAPIResponse(PS3OP.DISSYSCALL, res.code, res.response)
    }

    /**
     * Modes
     * @return Whether this ps3 has the syscall availiable for usage
     * @see
     */
    suspend fun checkSysCall(mode: Int): Boolean {
        val s = "PS3 CHECKSYSCALL $mode"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return false)
        listener.onJMAPIResponse(PS3OP.CHECKSYSCALL, res.code, res.response)
        return java.lang.Boolean.valueOf(res.response)
    }

    /**
     * Checks whats the ps3 is capable of calling.
     * @return SYSCALL8MODE
     * @see com.mrsmyx.JMAPI.SYSCALL8MODE
     */
    suspend fun partialCheckSysCall(): PS3MAPIResponse.SYSCALL8MODE? {
        val s = "PS3 PCHECKSYSCALL8"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return null)
        listener.onJMAPIResponse(
            PS3OP.SYSCALL8MODE,
            res.code,
            PS3MAPIResponse.SYSCALL8MODE.values()[Integer.valueOf(res.response)].toString()
        )
        return PS3MAPIResponse.SYSCALL8MODE.values()[Integer.valueOf(res.response)]
    }

    /**
     * Deletes the ps3 sys history
     * @param mode Include Dir or Exclude?
     * @return whether it was a success
     * @see
     */
    suspend fun deleteHistory(mode: DeleteHistory): Boolean {
        var s = "PS3 DELHISTORY"
        when (mode) {
            DeleteHistory.EXCLUDE_DIR -> {}
            DeleteHistory.INCLUDE_DIR -> s += "+D"
        }
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return false)
        listener.onJMAPIResponse(
            PS3OP.DELHISTORY,
            res.code,
            mode.toString() + " : " + res.response
        )
        return res.code === PS3MAPIResponse.Code.COMMANDOK
    }

    suspend fun checkSysCall() {
        //TODO : not implemented yet
    }

    /**
     * Gets all the processes that the ps3 is running
     * @return All process running on the ps3
     * @see PS3Process
     */
    val pids: List<Process>

    /**
     * Gets the firmware type of your ps3
     * @return Firmware Type, CEX COBRA, DEX COBRA?
     * @see
     */
    val fwType: String?
        get() {
            val s = "PS3 GETFWTYPE"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return null)
            listener.onJMAPIResponse(PS3OP.FWTYPE, res.code, res.response)
            return res.response
        }

    suspend fun getVersion(version: VERSION): PS3MAPIResponse? {
        val s = "$version GETVERSION"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return null)
        listener.onJMAPIResponse(PS3OP.SCVERSION, res.code, res.response)
        return res
    }

    suspend fun setBinaryMode(bMode: Boolean) {
        val (_, _, response) = PS3MAPIResponse.parse(
            sendAndRecv("TYPE" + if (bMode) " I" else " A") ?: return
        )
        println(response)
    }

    suspend fun getMinVersion(version: VERSION): PS3MAPIResponse? {
        val s = "$version GETMINVERSION"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(s) ?: return null)
        listener.onJMAPIResponse(PS3OP.SCMINVERSION, res.code, res.response)
        return res
    }

    @Throws(Exception::class)
    suspend fun buzzer(buzz: Buzzer) {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val buzzer = "PS3 BUZZER ${buzz.ordinal}"
        val r: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(buzzer) ?: return)
        listener.onJMAPIResponse(PS3OP.BUZZ, r.code, "A buzz was sent to the ps3")
        println(r.response)
    }

    @Throws(Exception::class)
    override fun boot(ps3boot: Boot) {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val boot = "PS3 $ps3boot"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(boot) ?: return)
        val msg = when (ps3boot) {
            Boot.REBOOT -> "Rebooting console"
            Boot.SHUTDOWN -> "Shutting down console"
            Boot.HARDREBOOT -> "Hard Rebooting console"
            Boot.SOFTREBOOT -> "Soft rebooting console"
        }
        listener.onJMAPIResponse(PS3OP.BOOT, res.code, msg)
        println(res.response)
    }

    @Throws(Exception::class)
    override fun notify(message: String) {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val notify = "PS3 NOTIFY $message"
        val (_, _, response) = PS3MAPIResponse.parse(sendAndRecv(notify) ?: return)
        println(response)
    }

    @Throws(Exception::class)
    suspend fun changeLed(
        color: LedColor,
        mode: LedStatus
    ): PS3MAPIResponse? {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val led = "PS3 LED ${color.ordinal} ${mode.ordinal}"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(led) ?: return null)
        println(res.code)
        listener.onJMAPIResponse(PS3OP.LED, res.code, res.response)
        return res
    }

    val info: ConsoleInfo?
        get() {
            val fw = fwVersion
            val type = fwType
            val t = temp
            if (fw != null && type != null && t != null) {
                return ConsoleInfo(fw, ConsoleType.parse(type), t)
            }
            return null
        }

    @get:Throws(Exception::class)
    var idps: String?
        get() {
            if (!isConnected) {
                throw Exception("Not connected to host.")
            }
            val idps = "PS3 GET${ConsoleId.IDPS.name.uppercase()}"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(idps) ?: return null)
            println(res.code)
            listener.onJMAPIResponse(PS3OP.IDPS, res.code, res.response)
            return res.response
        }
        set(value) {
            if (value != null) {
                if (!isConnected) throw Exception("Not connected to host.")
                val idps_cmd = "PS3 SET${ConsoleId.IDPS.name.uppercase()} ${
                    value.substring(
                        0,
                        16
                    )
                } ${value.substring(16)}"
                val res: PS3MAPIResponse =
                    PS3MAPIResponse.parse(sendAndRecv(idps_cmd) ?: return)
                listener.onJMAPIResponse(PS3OP.IDPSSET, res.code, res.response)
            }
        }

    @get:Throws(Exception::class)
    var psid: String?
        get() {
            if (!isConnected) {
                throw Exception("Not connected to host.")
            }
            val psid = "PS3 GET${ConsoleId.PSID.name.uppercase()}"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(psid) ?: return null)
            println(res.code)
            listener.onJMAPIResponse(PS3OP.PSID, res.code, res.response)
            return res.response
        }
        set(value) {
            if (value != null) {
                if (!isConnected) throw Exception("Not connected to host.")
                val psid_cmd = "PS3 SET${ConsoleId.PSID.name.uppercase()} ${
                    value.substring(
                        0,
                        16
                    )
                } ${value.substring(16)}"
                val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(psid_cmd) ?: return)
                listener.onJMAPIResponse(PS3OP.PSIDSET, res.code, res.response)
            }
        }

    fun disconnect(): Boolean {
        if (!isConnected) return true
        try {
            sendAndRecv("DISCONNECT")
            //processList = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return try {
            super.close()
            listener.onJMAPIResponse(
                PS3OP.DISCONNECTED,
                PS3MAPIResponse.Code.COMMANDOK,
                "Disconnected from ps3"
            )
            true
        } catch (e: IOException) {
            e.printStackTrace()
            listener.onJMAPIError("Something happened while disconnecting")
            false
        }
    }

    val isConnected: Boolean
        get() = if (socket.isConnected) {
            true
        } else {
            attached = true
            process = null
            false
        }

    @get:Throws(Exception::class)
    val temp: Temperature?
        get() {
            val psid = "PS3 GETTEMP"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(sendAndRecv(psid) ?: return null)
            println(res.response)
            return if (!res.response.contains(":")) {
                val temp = res.response.split("\\|".toRegex()).toTypedArray()
                listener.onJMAPITemperature(
                    res.code, Temperature.instantiate(
                        temp[0], temp[1]
                    )
                )
                Temperature.instantiate(temp[0], temp[1])
            } else {
                throw Exception("Error: could not obtain temperature")
            }
        }

    enum class PS3OP {
        IDPS, PSID, LED, DELHISTORY, BUZZ, DISSYSCALL, SYSCALL8MODE, CHECKSYSCALL, NETWORK_FOUND, DISCONNECTED, ERROR, FWTYPE, FWVERSION, SCMINVERSION, PSIDSET, IDPSSET, BOOT, SCVERSION
    }


}