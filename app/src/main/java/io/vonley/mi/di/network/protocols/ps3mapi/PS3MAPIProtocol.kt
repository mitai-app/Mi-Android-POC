package io.vonley.mi.di.network.protocols.ps3mapi

import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.ccapi.models.ConsoleIdType
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.common.*
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.common.cmds.LedColor
import io.vonley.mi.di.network.protocols.common.cmds.LedStatus
import io.vonley.mi.di.network.protocols.common.models.ConsoleInfo
import io.vonley.mi.di.network.protocols.ps3mapi.cmds.*
import io.vonley.mi.di.network.protocols.ps3mapi.models.PS3MAPIResponse
import io.vonley.mi.di.network.protocols.ps3mapi.models.Process
import io.vonley.mi.di.network.protocols.ps3mapi.models.Temperature
import io.vonley.mi.models.enums.ConsoleType
import io.vonley.mi.models.enums.Feature
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

interface PS3MAPIProtocol : PSXProtocol {

    override val feature: Feature get() = Feature.PS3MAPI
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    val listener: JMAPIListener
    val processes: List<Process>
    val liveProcesses: LiveData<List<Process>>
    var attached: Boolean
    var process: Process?



    fun connect(ip: String?, port: Int): Boolean {
        return if (!socket.isConnected() || socket.isClosed()) {
            try {
                val br = BufferedReader(InputStreamReader(socket.getInputStream()))
                val first: PS3MAPIResponse = PS3MAPIResponse.parse(br.readLine())
                if (first.success && first.code === PS3MAPIResponse.Code.PS3MAPICONNECTED) {
                    val second: PS3MAPIResponse = PS3MAPIResponse.parse(br.readLine())
                    if (second.success && second.code === PS3MAPIResponse.Code.PS3MAPICONNECTEDOK) {
                        println("Connection Established!")
                        return true
                    }
                    return true
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        } else false
    }

    @Throws(IOException::class) // JMAPIException::class)
    fun openDataSocket()

    @Throws(Exception::class)
    fun connectDataSocket()
    fun closeDataSocket(): Boolean

    interface JMAPIListener {

        fun onJMAPIError(error: String?)
        fun onJMAPIResponse(ps3Op: PS3OP?, responseCode: PS3MAPIResponse.Code?, message: String?)
        fun onJMAPIPS3Process(
            responseCode: PS3MAPIResponse.Code?,
            processes: List<Process>?
        )

        fun onJMAPITemperature(responseCode: PS3MAPIResponse.Code?, temperature: Temperature?)
    }

    interface JMAPIMemoryListener {
        @Throws(Exception::class)
        fun onJMAPIGetMemory(
            process: String?,
            offset: Long,
            size: Int,
            signedBytes: ByteArray?,
            unsignedBytes: IntArray?
        )

        fun onJMAPISetMemory(response: String?, offset: Long)
        fun onJMAPIMemoryError(response: String?, process: String?, offset: Long)
    }

    /**
     * Attaches to the ps3 process with the process id provided by developer
     *
     * @param process Attaches to the process
     * @return whether the process attachment was successful
     * @see PS3Process
     */
    fun attach(process: Process?): Boolean {
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
    fun smartAttach(): Boolean {
        this.process = smartContainsProcess(pids)
        attached = process != null
        return attached
    }

    fun smartContainsProcess(processes: List<Process>): Process? =
        processes.find { p -> p.title.lowercase().contains("eboot") }

    fun containsProcess(processes: List<Process>, process: Process?): Boolean {
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
    fun detach() {
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
            val response = super.send(s) ?: return null
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
    fun disableSysCall(mode: PS3MAPIResponse.SYSCALL8MODE) {
        val s = "PS3 DISABLESYSCALL " + mode.ordinal.toString()
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return)
        res.response
        listener.onJMAPIResponse(PS3OP.DISSYSCALL, res.code, res.response)
    }

    /**
     * Modes
     * @return Whether this ps3 has the syscall availiable for usage
     * @see
     */
    fun checkSysCall(mode: Int): Boolean {
        val s = "PS3 CHECKSYSCALL $mode"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return false)
        listener.onJMAPIResponse(PS3OP.CHECKSYSCALL, res.code, res.response)
        return java.lang.Boolean.valueOf(res.response)
    }

    /**
     * Checks whats the ps3 is capable of calling.
     * @return SYSCALL8MODE
     * @see com.mrsmyx.JMAPI.SYSCALL8MODE
     */
    fun partialCheckSysCall(): PS3MAPIResponse.SYSCALL8MODE? {
        val s = "PS3 PCHECKSYSCALL8"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return null)
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
    fun deleteHistory(mode: DeleteHistory): Boolean {
        var s = "PS3 DELHISTORY"
        when (mode) {
            DeleteHistory.EXCLUDE_DIR -> {}
            DeleteHistory.INCLUDE_DIR -> s += "+D"
        }
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return false)
        listener.onJMAPIResponse(
            PS3OP.DELHISTORY,
            res.code,
            mode.toString() + " : " + res.response
        )
        return res.code === PS3MAPIResponse.Code.COMMANDOK
    }

    fun checkSysCall() {
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
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return null)
            listener.onJMAPIResponse(PS3OP.FWTYPE, res.code, res.response)
            return res.response
        }

    fun getVersion(version: VERSION): PS3MAPIResponse? {
        val s = "$version GETVERSION"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return null)
        listener.onJMAPIResponse(PS3OP.SCVERSION, res.code, res.response)
        return res
    }

    fun setBinaryMode(bMode: Boolean) {
        val (_, _, response) = PS3MAPIResponse.parse(
            super.send("TYPE" + if (bMode) " I" else " A") ?: return
        )
        println(response)
    }

    fun getMinVersion(version: VERSION): PS3MAPIResponse? {
        val s = "$version GETMINVERSION"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(s) ?: return null)
        listener.onJMAPIResponse(PS3OP.SCMINVERSION, res.code, res.response)
        return res
    }

    @Throws(Exception::class)
    fun buzzer(buzz: Buzzer) {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val buzzer = "PS3 BUZZER ${buzz.ordinal}"
        val r: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(buzzer) ?: return)
        listener.onJMAPIResponse(PS3OP.BUZZ, r.code, "A buzz was sent to the ps3")
        println(r.response)
    }

    @Throws(Exception::class)
    override fun boot(ps3boot: Boot) {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val boot = "PS3 $ps3boot"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(boot) ?: return)
        val msg  = when (ps3boot) {
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
        val (_, _, response) = PS3MAPIResponse.parse(super.send(notify) ?: return)
        println(response)
    }

    @Throws(Exception::class)
    fun changeLed(
        color: LedColor,
        mode: LedStatus
    ): PS3MAPIResponse? {
        if (!isConnected) {
            throw Exception("Not connected to host.")
        }
        val led = "PS3 LED ${color.ordinal} ${mode.ordinal}"
        val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(led) ?: return null)
        println(res.code)
        listener.onJMAPIResponse(PS3OP.LED, res.code, res.response)
        return res
    }

    val info: ConsoleInfo?
        get() {
            val fw = fwVersion
            val type = fwType
            val t = temp
            if(fw != null && type != null && t != null){
                return ConsoleInfo(fw, io.vonley.mi.di.network.protocols.ccapi.models.ConsoleType.parse(type), t)
            }
            return null
        }

    @get:Throws(Exception::class)
    var idps: String?
        get() {
            if (!isConnected) {
                throw Exception("Not connected to host.")
            }
            ConsoleIdType.IDPS
            val idps = "PS3 GETIDPS"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(idps) ?: return null)
            println(res.code)
            listener.onJMAPIResponse(PS3OP.IDPS, res.code, res.response)
            return res.response
        }
        set(value) {
            if (value != null) {
                if (!isConnected) throw Exception("Not connected to host.")
                val idps_cmd = "PS3 SETIDPS " + value.substring(0, 16) + " " + value.substring(16)
                val res: PS3MAPIResponse =
                    PS3MAPIResponse.parse(super.send(idps_cmd) ?: return)
                listener.onJMAPIResponse(PS3OP.IDPSSET, res.code, res.response)
            }
        }

    @get:Throws(Exception::class)
    var psid: String?
        get() {
            if (!isConnected) {
                throw Exception("Not connected to host.")
            }
            val psid = "PS3 GETPSID"
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(psid) ?: return null)
            println(res.code)
            listener.onJMAPIResponse(PS3OP.PSID, res.code, res.response)
            return res.response
        }
        set(value) {
            if (value != null) {
                if (!isConnected) throw Exception("Not connected to host.")
                val psid_cmd = "PS3 SETPSID " + value.substring(0, 16) + " " + value.substring(16)
                val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(psid_cmd) ?: return)
                listener.onJMAPIResponse(PS3OP.PSIDSET, res.code, res.response)
            }
        }

    fun disconnect(): Boolean {
        if (!isConnected) return true
        try {
            super.send("DISCONNECT")
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
            val res: PS3MAPIResponse = PS3MAPIResponse.parse(super.send(psid) ?: return null)
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