package io.vonley.mi.di.network.protocols.ccapi

import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.common.cmds.LedColor
import io.vonley.mi.di.network.protocols.common.cmds.LedStatus
import io.vonley.mi.di.network.protocols.common.models.*
import io.vonley.mi.models.enums.Feature
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.net.URLEncoder

enum class CCAPIERROR {
    NOT_ATTACHED
}

class CCAPIException(error: CCAPIERROR, errorMsg: String) : Throwable() {
    private val error: CCAPIERROR
    private val errorMsg: String
    override val message: String
        get() = String.format("%s\n%s", localizedMessage, stackTrace.toString())

    override fun getLocalizedMessage(): String {
        return "${error.name}: $errorMsg"
    }

    override fun toString(): String {
        return localizedMessage
    }

    init {
        this.error = error
        this.errorMsg = errorMsg
    }
}

interface CCAPIProtocol : PSXProtocol {


    /**
     * Gets the process running on the ps3
     * @see Process
     * @return All the process that the PS3 has running.
     * @throws IOException
     */
    @get:Throws(IOException::class)
    val pids: List<Process>

    override val feature: Feature get() = Feature.CCAPI
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    val processes: List<Process>
    val liveProcesses: LiveData<List<Process>>
    var attached: Boolean
    var process: Process?
    val TAG: String get() = CCAPIProtocol::class.java.name

    val urlStub get() = "http://${service.targetIp}:${feature.ports.first()}/ccapi/"

    fun compileUrl(compiled: String): String {
        return "$urlStub$compiled"
    }

    @Throws(IOException::class)
    fun getSimpleRequest(urlString: String): String? {
        return getRequest(urlString).body?.string()
    }

    @Throws(IOException::class)
    fun getListRequest(urlString: String): List<String> {
        return BufferedReader(
            InputStreamReader(
                getRequest(urlString).body?.byteStream() ?: return emptyList()
            )
        ).use { br ->
            val stringList: MutableList<String> = ArrayList()
            var stub: String? = null
            while (br.readLine().also { stub = it } != null) {
                stringList.add(stub ?: break)
            }
            return@use stringList
        }
    }

    /**
     * Set the mode for the ps3
     * @param boot SHUTDOWN, SOFTBOOT, HARDBOOT
     * @throws IOException
     */
    override fun boot(ps3boot: Boot) {
        getSimpleRequest(compileUrl(CCAPIUrlBuilder.shutDown(ps3boot)))
    }

    override fun notify(message: String) {
        getSimpleRequest(
            compileUrl(
                CCAPIUrlBuilder.notify(
                    NotifyIcon.INFO,
                    URLEncoder.encode(message, "UTF-8").replace("+", "%20")
                )
            )
        );
    }

    /**
     * Gets the current temperature
     * @see Temperature
     *
     * @return The RSX & CELL Temperature of the PS3 in both Celsius and Fahrenheit
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getTemperature(): Temperature {
        val temp: List<String> = getListRequest(compileUrl(CCAPIUrlBuilder.temperature))
        return Temperature(
            Integer.decode("0x" + temp[1]).toString(),
            Integer.decode("0x" + temp[2]).toString()
        )
    }

    /**
     * Send a message to the ps3
     * @param notifyicons A Selection of notify icons you can choose from
     * @param message A Message you want to send to the ps3
     * @return 0
     * @throws IOException
     */
    @Throws(IOException::class)
    fun notify(notifyicons: NotifyIcon, message: String): String? {
        return getSimpleRequest(
            compileUrl(
                CCAPIUrlBuilder.notify(
                    notifyicons,
                    URLEncoder.encode(message, "UTF-8").replace("+", "%20")
                )
            )
        )
    }

    /**
     * Set the led color and status (on or off)
     * @param color Colors of the ps3 LED
     * @param led OFF or ON
     * @return 0
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setConsoleLed(color: LedColor, led: LedStatus): String? {
        return getSimpleRequest(compileUrl(CCAPIUrlBuilder.setConsoleLed(color, led)))
    }

    /**
     * Set the psid or idps of the ps3
     * @param type IDPS or PSID
     * @param id The ID here
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setConsoleIds(type: ConsoleId, id: String?): String? {
        return getSimpleRequest(compileUrl(CCAPIUrlBuilder.setConsoleIds(type, id)))
    }

    /**
     * Set the psid of idps of the ps3 on boot or remove the onboot trigger
     * @param type IDPS or PSID
     * @param onBoot OnBoot or Turn off OnBoot.
     * @param id The ID Here
     * @return 0
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setBootConsoleIds(type: ConsoleId, onBoot: Boolean, id: String?): String? {
        return getSimpleRequest(
            compileUrl(
                CCAPIUrlBuilder.setBootConsoleIds(
                    type,
                    onBoot,
                    id
                )
            )
        )
    }


    /**
     * Send a buzz to the ps3
     * @param buzzer Continuous, Single, Double, Triple
     * @return 0
     * @throws IOException
     */
    @Throws(IOException::class)
    fun ringBuzzer(buzzer: Buzzer): String? {
        return getSimpleRequest(compileUrl(CCAPIUrlBuilder.ringBuzzer(buzzer)))
    }

    /**
     * Attaches to the game process
     * @return Whether the attachment was successful
     * @throws IOException
     */
    @Throws(IOException::class)
    fun attach(): Boolean {
        for (process in pids) {
            if (process.name.contains("EBOOT.BIN")) {
                attach(process)
                return true
            }
        }
        detach()
        return false
    }

    fun attach(pid: Process) {
        process = pid
    }

    /**
     * Detaches from the ps3.
     */
    fun detach() {
        process = null
    }

    /**
     * @return If ps3 is currently attached to a process id.
     */
    fun isProcessAttached(): Boolean {
        return process != null
    }

    /**
     * @param pid Process ID
     * @param addr Address
     * @param value Value you want to set
     * @return 0
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setMemory(pid: String, addr: String, value: String): String? {
        return getSimpleRequest(compileUrl(CCAPIUrlBuilder.setMemory(pid, addr, value)))
    }

    /**
     *
     * For those who rather set memory using the char[], byte is signed (annoying)
     * @param addr 0x Format
     * @param value char array format
     * @return 0
     * @throws CCAPIException
     * @throws IOException
     */
    @Throws(CCAPIException::class, IOException::class)
    fun setMemory(addr: Int, value: CharArray?): String? {
        return if (process != null) {
            //setMemory(process!!.process, Integer.toHexString(addr), Hex.encodeHexString(value, false))
            ""
        } else {
            throw CCAPIException(CCAPIERROR.NOT_ATTACHED, "You are not attached to any processes")
        }
    }

    /**
     *
     * For those who prefer to use byte[] over char[], ie. (byte)0xFF
     * @param addr 0x format
     * @param value byte array format, value you want to set
     * @return
     * @throws CCAPIException
     * @throws IOException
     */
    @Throws(CCAPIException::class, IOException::class)
    fun setMemory(addr: Int, value: ByteArray?): String? {
        return if (process != null) {
            //setMemory(process, Integer.toHexString(addr), Hex.encodeHexString(value, false))
            ""
        } else {
            throw CCAPIException(CCAPIERROR.NOT_ATTACHED, "You are not attached to any processes")
        }
    }

    /**
     *
     * Get the memory of the ps3
     * @param addr 0x format
     * @param size size you want to get
     * @return memory byte[]
     * @throws Exception
     * @throws CCAPIException
     */
    @Throws(Exception::class, CCAPIException::class)
    fun getMemory(addr: Int, size: Int): ByteArray? {
        val s = getMemoryString(Integer.toHexString(addr), size) ?: return null
        val bytes = ByteArray(s.length / 2)
        var i = 0
        while (i < s.length) {
            bytes[i / 2] = (s[i].toString() + s[i + 1].toString()).toLong(16).toInt().toByte()
            i += 2
        }
        return bytes
    }

    /**
     *
     * Get the memory of the ps3
     * @param addr 0x format
     * @param size size you want to recieve
     * @return char[] memory
     * @throws Exception
     * @throws CCAPIException
     */
    @Throws(Exception::class, CCAPIException::class)
    fun getMemoryTC(addr: Int, size: Int): CharArray? {
        val s = getMemoryString(Integer.toHexString(addr), size) ?: return null
        val chars = CharArray(s.length / 2)
        var i = 0
        while (i < s.length) {
            chars[i / 2] = (s[i].toString() + s[i + 1].toString()).toLong(16).toInt().toChar()
            i += 2
        }
        return chars
    }

    @Throws(IOException::class)
    fun getMemoryString(pid: String?, addr: String, size: Int): String? {
        return getSimpleRequest(
            compileUrl(
                CCAPIUrlBuilder.getMemory(
                    pid,
                    addr,
                    size
                )
            )
        )?.substring(1)
    }

    @Throws(CCAPIException::class, IOException::class)
    fun getMemoryString(addr: String, size: Int): String? {
        return if (process != null) {
            getMemoryString(process!!.process, addr, size)
        } else {
            throw CCAPIException(CCAPIERROR.NOT_ATTACHED, "You are not attached to any processes")
        }
    }

    /**
     * Receives the console information
     * @return Console Information
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @see ConsoleInfo
     */
    @Throws(IOException::class, IndexOutOfBoundsException::class)
    fun getConsoleInfo(): ConsoleInfo? {
        val temperature = getTemperature()
        val firmware = getFirmwareInfo()
        var firm: String = ""
        var occur = 0
        for (c in firmware[1].substring(0, 4).toCharArray()) {
            if (c == '0' && occur < 1) {
                firm += "."
                occur++
            } else firm += c
        }
        return ConsoleInfo(firm, ConsoleType.values()[firmware[3].toInt()], temperature)
    }

    //region Enumerations
    enum class NotifyIcon {
        INFO, CAUTION, FRIEND, SLIDER, WRONGWAY, DIALOG, DIALOGSHADOW, TEXT, POINTER, GRAB, HAND, PEN, FINGER, ARROW, ARROWRIGHT, PROGRESS, TROPHY1, TROPHY2, TROPHY3, TROPHY4
    }

    //endregion

    //endregion
    //region Private Methods
    @Throws(IOException::class)
    fun getFirmwareInfo(): List<String> {
        return getListRequest(compileUrl(CCAPIUrlBuilder.firmWareInfo))
    }

    @Throws(IOException::class)
    fun getProcessList(): List<String> {
        return getListRequest(compileUrl(CCAPIUrlBuilder.processList))
    }

    @Throws(IOException::class)
    fun getProcessName(pid: String): List<String> {
        return getListRequest(compileUrl(CCAPIUrlBuilder.getProcessName(pid)))
    }

    //endregion

    //endregion
    private object CCAPIUrlBuilder {
        val firmWareInfo: String
            get() = DIR.GETFIRMWAREINFO.name.lowercase()
        val temperature: String
            get() = DIR.GETTEMPERATURE.name.lowercase()
        val processList: String
            get() = DIR.GETPROCESSLIST.name.lowercase()

        fun getProcessName(pid: String): String {
            return DIR.GETPROCESSNAME.name.toLowerCase() + "?pid=$pid"
        }

        fun shutDown(boot: Boot): String {
            return DIR.SHUTDOWN.name.toLowerCase() + "?mode=${boot.code}"
        }

        fun notify(notifyicons: NotifyIcon, message: String): String {
            return DIR.NOTIFY.name.lowercase() + "?id=${notifyicons.ordinal}&msg=${
                message.replace(
                    " ",
                    "%20"
                )
            }"
        }

        fun setConsoleLed(color: LedColor, led: LedStatus): String {
            return DIR.SETCONSOLELED.name.lowercase() + "?color=${color.ordinal}&status=${led}"
        }

        fun setConsoleIds(type: ConsoleId, id: String?): String {
            return DIR.SETCONSOLEIDS.name.lowercase() + "?type=${type.ordinal}&id=$id"

        }

        fun setBootConsoleIds(type: ConsoleId, onBoot: Boolean, id: String?): String {
            return DIR.SETBOOTCONSOLEIDS.name.toLowerCase() + "?type=${type.ordinal}&on=${if (onBoot) 1 else 0}&id=$id"

        }

        fun ringBuzzer(buzzer: Buzzer): String {
            return DIR.RINGBUZZER.name.lowercase() + "?type=${buzzer.ordinal}"
        }

        fun setMemory(pid: String?, addr: String?, value: String?): String {
            return DIR.SETMEMORY.name.lowercase() + "?pid=$pid&addr=$addr&value=$value"

        }

        fun getMemory(pid: String?, addr: String?, size: Int): String {
            return DIR.GETMEMORY.name.lowercase() + "?pid=$pid&addr=$addr&size=$size"
        }

        private enum class DIR {
            GETFIRMWAREINFO, SETBOOTCONSOLEIDS, SETCONSOLEIDS, SHUTDOWN, GETTEMPERATURE, GETPROCESSLIST, SETMEMORY, GETMEMORY, GETPROCESSNAME, SETCONSOLELED, NOTIFY, RINGBUZZER
        }
    }

}