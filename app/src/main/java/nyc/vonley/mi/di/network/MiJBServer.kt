package nyc.vonley.mi.di.network

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import nyc.vonley.mi.BuildConfig
import java.io.File
import java.net.Socket
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext

class MiJBServer constructor(
    @ApplicationContext context: Context,
    val service: PSXService
) : NanoHTTPD(8080),
    CoroutineScope {

    private val routes: HashMap<String, ByteArray> = hashMapOf()
    private val payloads: HashMap<String, ByteArray> = hashMapOf()

    private val failHtml: ByteArray = context.assets.open("pages/fail.html").readBytes()

    private val root: String
        get() {
            return "root/"
        }

    private var ready: Boolean = false

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var console: Device? = null

    companion object {
        const val TAG = "MiJbServer"
    }

    fun init(context: Context) {
        val open = context.assets.open("jb/ps4.zip")
        payloads["6.72"] to context.assets.open("payloads/gh2/672.bin").readBytes()
        payloads["7.02"] to context.assets.open("payloads/gh2/702.bin").readBytes()
        payloads["7.50"] to context.assets.open("payloads/gh2/750.bin").readBytes()
        payloads["7.51"] to context.assets.open("payloads/gh2/751.bin").readBytes()
        payloads["7.55"] to context.assets.open("payloads/gh2/755.bin").readBytes()
        payloads["9.00"] to context.assets.open("payloads/gh2/900.bin").readBytes()
        launch {
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
            start()
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
        Log.e(TAG, "Fetching: $s")
        return routes[s] ?: ByteArray(0)
    }

    private fun createDir(name: String?) {
        val file = File(name)
        if (!file.isDirectory) {
            file.mkdirs()
        }
    }

    data class Device(
        val device: String,
        val version: String,
        var ip: String? = null
    )

    fun parse(string: String): Device? {
        val extractPlaystation = """\(([^()]*)\)""".toRegex()
        val extractVersion = "([0-9]+(?:\\.[0-9]+)?)".toRegex()
        val matchEntire = extractPlaystation.findAll(string).flatMap { it.groupValues }.distinct()
            .filter { it.contains("playstation", true) }.toList().firstOrNull();
        if (matchEntire != null) {
            println(matchEntire)
            val extracted =
                extractVersion.findAll(matchEntire).flatMap { it.groupValues }.distinct().toList()
            val version = extracted.last()
            return Device(matchEntire, version)
        }
        return null
    }

    fun parse(session: IHTTPSession): Device? {
        val s = session.headers["user-agent"]
        return parse(s.toString())?.apply {
            this.ip = session.headers["http-client-ip"]
        }
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
                callback.onDeviceConnected(it)
                val supported = when (it.version) {
                    "6.72", "7.02", "7.50", "7.51", "7.55", "9.00" -> true
                    else -> it.version.contains("7.5")
                }
                if (supported) {
                    if (this.console?.ip != it.ip) this.console = it
                    callback.onLog("This device is supported")
                    callback.onLog("PS4 -> $uri")
                    when (uri) {
                        "/" -> {
                            return Response(
                                Response.Status.OK,
                                "text/html",
                                read("index.html")
                            )
                        }
                        "/log/done" -> {
                            callback.onLog("Jailbreak completed!")
                            callback.onFinishedJb()
                            val outSock = Socket(it.ip, 9020)
                            outSock.getOutputStream().use { sock ->
                                with(sock) {
                                    val payloads = payloads[it.version]
                                    callback.onLog("Sending GoldenHen 2.0b2 payload for ${it.version}")
                                    write(payloads)
                                    flush()
                                    callback.onPayloadSent()
                                }
                            }
                        }
                        else -> {
                            val path = uri.drop(1)
                            val mime = getMimeType(uri) ?: "text/*"
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

    interface MiJbServerListener {
        fun onDeviceConnected(device: Device)
        fun onLog(string: String)
        fun onFinishedJb()
        fun onPayloadSent()
        fun onUnsupported(s: String)
    }

    private val callbacks: HashMap<Class<*>, MiJbServerListener> = hashMapOf()

    fun add(jb: MiJbServerListener) {
        callbacks[jb.javaClass] = jb
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

        override fun onFinishedJb() {
            val name = "onFinishedJb"
            launch {
                withContext(Dispatchers.Main) {
                    callbacks.onEach { a ->
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Calling ${a.key.name}::${name}()")
                        }
                        a.value.onFinishedJb()
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
    }

}