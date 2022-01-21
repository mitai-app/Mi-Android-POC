package nyc.vonley.mi.di.network

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.extensions.fromJson
import nyc.vonley.mi.models.Device
import nyc.vonley.mi.models.Mi
import nyc.vonley.mi.utils.SharedPreferenceManager
import java.io.File
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext

class MiJBServer constructor(
    @ApplicationContext val context: Context,
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    val service: PSXService
) : NanoHTTPD(manager.jbPort),
    CoroutineScope {


    val Device.jbPath: String
        get() {
            return when (version) {
                "5.05" -> "jb/505/"
                "6.72" -> "jb/672/"
                "7.02" -> "jb/702/"
                "7.50",
                "7.51",
                "7.55" -> "jb/75x/"
                "9.00" -> "jb/900/"
                else -> "pages/fail.html"
            }
        }

    val Device.supported
        get() = when (version) {
            "6.72", "7.02", "7.50", "7.51", "7.55", "9.00" -> true
            else -> false
        }

    private val miJs: ByteArray = context.assets.open("jb/mi.js").readBytes()
    private val failHtml: ByteArray = context.assets.open("pages/fail.html").readBytes()

    private val routes: HashMap<String, ByteArray> = hashMapOf()
    private val payloads: HashMap<String, ByteArray> = hashMapOf()


    private val root: String
        get() {
            return console!!.jbPath
        }

    private var ready: Boolean = false

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var console: Device? = null

    companion object {
        const val TAG = "MiJbServer"
    }


    fun extract(open: InputStream) {
        ZipInputStream(open).use { zip ->
            with(zip) {
                var stub: ZipEntry? = null
                while (nextEntry.also { stub = it } != null) {
                    val ze: ZipEntry = stub!!
                    Log.d(TAG, "Decompressing + ${ze.name}")
                    if (ze.isDirectory) {
                        createDir(ze.name)
                    } else {
                        val name = ze.name
                        if (name.contains("__MACOSX")) continue
                        //val file = File(path, name)
                        Log.e(TAG, "Extracting: $name")
                        try {
                            routes[name] = readBytes()
                            closeEntry()
                            /*
                            file.outputStream().use { fos ->
                                with(fos) {
                                    write(readBytes())
                                }
                                closeEntry()
                            }*/
                        } catch (e: Throwable) {
                            Log.e(TAG, e.message ?: "Unable to extract")
                            closeEntry()
                        }
                    }
                }
            }
        }
    }

    fun init(context: Context) {
        payloads["6.72"] = context.assets.open("payloads/goldenhen/672.bin").readBytes()
        payloads["7.02"] = context.assets.open("payloads/goldenhen/702.bin").readBytes()
        payloads["7.50"] = context.assets.open("payloads/goldenhen/750.bin").readBytes()
        payloads["7.51"] = context.assets.open("payloads/goldenhen/751.bin").readBytes()
        payloads["7.55"] = context.assets.open("payloads/goldenhen/755.bin").readBytes()
        payloads["9.00"] = context.assets.open("payloads/goldenhen/900.bin").readBytes()
        launch {
            try {
                start()
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.message ?: "Hmmm")
                }
            }
        }
    }


    override fun start() {
        ready = true
        super.start()
    }


    override fun stop() {
        ready = false
        super.stop()
    }

    private fun read(path: String): String {
        return readBytes(path).decodeToString()
    }

    private fun readBytes(path: String): ByteArray {
        val s = root + path
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "Fetching: $s")
        }
        return context.assets.open(s).readBytes()
    }

    private fun createDir(name: String?) {
        val file = File(name)
        if (!file.isDirectory) {
            file.mkdirs()
        }
    }



    private fun parse(session: IHTTPSession): Device? {
        val string = session.headers["user-agent"].toString()
        val extractPlaystation = """\(([^()]*)\)""".toRegex()
        val extractVersion = "([0-9]+(?:\\.[0-9]+)?)".toRegex()
        val matchEntire = extractPlaystation.findAll(string).flatMap { it.groupValues }.distinct()
            .filter { it.contains("playstation", true) }.toList().firstOrNull();
        if (matchEntire != null) {
            println(matchEntire)
            val extracted =
                extractVersion.findAll(matchEntire).flatMap { it.groupValues }.distinct().toList()
            val version = extracted.last()
            return Device(matchEntire, version, session.headers["http-client-ip"] ?: "0.0.0.0")
        }
        return null
    }


    private fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type;
    }


    override fun serve(s: IHTTPSession?): Response {
        s?.let { session ->
            val uri = session.uri.toString()
            val console = parse(session)
            console?.let {
                manager.ftpPass
                callback.onDeviceConnected(it)
                manager.targetVersion = it.version
                manager.targetName = it.ip
                if (it.supported) {
                    if (this.console?.ip != it.ip) this.console = it
                    val mime = getMimeType(uri) ?: "text/*"
                    callback.onLog("PS4 -> $uri")
                    when (uri) {
                        "/" -> {
                            return Response(
                                Response.Status.OK,
                                "text/html",
                                read("index.html")
                            )
                        }
                        "/mi.js" -> {
                            return Response(
                                Response.Status.OK,
                                "text/javascript",
                                miJs.decodeToString()
                            )
                        }
                        "/jb/cmd" -> {
                            Log.e(TAG, "Method: ${session.method.name}")
                            val map = HashMap<String, String>()
                            session.parseBody(map)
                            val body = session.queryParameterString
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, body)
                            }
                            val mi: Mi<Mi.Cmd> = body.fromJson() ?: return Response(
                                Response.Status.INTERNAL_ERROR,
                                "text/*",
                                "unable to parse body"
                            )
                            callback.onCommand(mi)
                            val cmd = mi.data.cmd
                            val message = mi.response
                            when (cmd) {
                                "jb.success" -> {
                                    callback.onLog("Jailbreak Completed")
                                    callback.onJailbreakSucceeded(message)
                                }
                                "jb.failed" -> {
                                    callback.onLog("Jailbreak Failed")
                                    callback.onJailbreakFailed(message)
                                }
                                "send.payload" -> {
                                    callback.onLog("Sending Payload")
                                    sendPayload(it)
                                }
                                else -> return Response(
                                    Response.Status.NOT_FOUND,
                                    "text/*",
                                    "invalid cmd"
                                )
                            }
                            return Response(Response.Status.OK, "text/*", "received")
                        }
                        else -> {
                            val path = uri.drop(1)
                            return Response(
                                Response.Status.OK,
                                mime,
                                read(path)
                            )
                        }
                    }
                } else {
                    return Response(Response.Status.OK, "text/html", failHtml.decodeToString())
                }
            } ?: run {
                callback.onUnsupported("This device is not supported!")
                return Response(
                    Response.Status.OK, "text/html", failHtml.decodeToString()
                )
            }
        }
        return super.serve(s)
    }


    lateinit var port9021: Job

    private fun sendPayload(device: Device) {
        val payload = payloads[device.version]
        val block: suspend CoroutineScope.() -> Unit = {
            delay(10000)
            while (true) {
                try {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Attempting to connect to ${device.ip}:9021")
                    }
                    val outSock = Socket()
                    val inetSocketAddress = InetSocketAddress(device.ip, 9021)
                    outSock.connect(inetSocketAddress, 10000)
                    val outputStream = outSock.getOutputStream()
                    callback.onLog("Sending GoldenHen 2.0b2 payload for PS4 ${device.version}")
                    outputStream.write(payload)
                    outputStream.flush()
                    outputStream.close()
                    callback.onPayloadSent()
                    outSock.close()
                    break
                } catch (ex: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Port not open, ${ex.message}")
                        Log.e(TAG, "Retrying in 5 seconds")
                    }
                    delay(10000)
                }
            }
        }
        if (!this::port9021.isInitialized) {
            this.port9021 = launch(block = block)
        } else if ((!this.port9021.isActive || this.port9021.isCompleted) && !this.port9021.start()) {
            this.port9021 = launch(block = block)
        }
    }

    interface MiJbServerListener {
        fun onDeviceConnected(device: Device)
        fun onLog(string: String)
        fun onJailbreakSucceeded(message: String)
        fun onJailbreakFailed(message: String)
        fun onPayloadSent()
        fun onUnsupported(s: String)
        fun onCommand(mi: Mi<Mi.Cmd>)
    }

    private val callbacks: HashMap<Class<*>, MiJbServerListener> = hashMapOf()

    fun add(jb: MiJbServerListener) {
        callbacks[jb.javaClass] = jb
    }

    fun stopService() {
        try {
            stop()
        } catch (e: Throwable) {
            Log.e(TAG, e.message ?: "Hmmm idk")
        }
    }

    fun remove(jb: MiJbServerListener) {
        if(callbacks.containsKey(jb.javaClass)){
            callbacks.remove(jb.javaClass)
        }
    }

    private val callback: MiJbServerListener = object : MiJbServerListener {
        override fun onDeviceConnected(device: Device) {
            val name = "onDeviceConnected"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / ${device.device}")
                        }
                        a.value.onDeviceConnected(device)
                    }
                }
            }
        }

        override fun onLog(string: String) {
            val name = "onLog"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string) / $string")
                        }
                        a.value.onLog(string)
                    }
                }
            }
        }

        override fun onJailbreakSucceeded(message: String) {
            val name = "onJailbreakSucceeded"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(message)")
                        }
                        a.value.onJailbreakSucceeded(message)
                    }
                }
            }
        }

        override fun onJailbreakFailed(message: String) {
            val name = "onJailbreakFailed"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(message)")
                        }
                        a.value.onJailbreakFailed(message)
                    }
                }
            }
        }

        override fun onPayloadSent() {
            val name = "onPayloadSent"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}()")
                        }
                        a.value.onPayloadSent()
                    }
                }
            }
        }

        override fun onUnsupported(s: String) {
            val name = "onUnsupported"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(string)")
                        }
                        a.value.onUnsupported(s)
                    }
                }
            }
        }

        override fun onCommand(mi: Mi<Mi.Cmd>) {
            val name = "onCommand"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}(Mi<Mi.Cmd>())")
                        }
                        a.value.onCommand(mi)
                    }
                }
            }
        }
    }

}