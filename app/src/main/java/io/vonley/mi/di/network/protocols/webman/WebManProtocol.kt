package io.vonley.mi.di.network.protocols.webman


import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIProtocol
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.ps3mapi.models.Process
import io.vonley.mi.models.enums.Feature
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.*
import java.util.*

data class Game(var title: String, var icon: String, var link: String, val info: String)


enum class GameType {
    PS3, PSP, PS2, PSX, QUERY
}


interface WebManProtocol : PSXProtocol {

    override val feature: Feature get() = Feature.WEBMAN
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    val listener: PS3MAPIProtocol.JMAPIListener
    val processes: List<Process>
    val liveProcesses: LiveData<List<Process>>
    var attached: Boolean
    var process: Process?
    val TAG: String get() = WebManProtocol::class.java.name


    private val gameUrl: String
        get() = "http://${service.targetIp}/dev_hdd0/xmlhost/game_plugin/mygames.xml"

    @Throws(IOException::class)
    fun searchGames(): Map<GameType, List<Game>> {
        val xml: String = gameUrl
        Log.e(TAG, xml)
        val file = download(xml)?.string() ?: return EnumMap(GameType::class.java)
        val document = Jsoup.parse(file, "", Parser.xmlParser())
        return hashMapOf(
            Pair(GameType.PS3, getPS3Games(document)),
            Pair(GameType.PS2, getPS2Games(document)),
            Pair(GameType.PSP, getPSPGames(document)),
            Pair(GameType.PSX, getPSXGames(document))
        )
    }

    @Throws(Exception::class)
    private fun getPSXGames(document: Document): List<Game> {
        val games = document.getElementById("seg_wm_psx_items")
        val tables = games.getElementsByTag("Table")
        val ps2Games: MutableList<Game> = ArrayList<Game>()
        for (table in tables) {
            if (table.attr("key") == "ps2_classic_launcher") continue
            val singleton = table.children()
            var title = ""
            var icon = ""
            var link = ""
            var info = ""
            for (s in singleton) {
                when {
                    s.attr("key") == "title" -> {
                        title = s.text()
                    }
                    s.attr("key") == "icon" -> {
                        icon = s.text()
                    }
                    s.attr("key") == "module_action" -> {
                        link = s.text().replace("127.0.0.1", service.targetIp ?: "")
                    }
                    s.attr("key") == "info" -> {
                        info = s.text()
                    }
                }
            }
            icon = try {
                downloadFile(
                    "http://" + service.targetIp + ":80" + icon.replace(" ", "%20"),
                    Environment.getExternalStorageDirectory().toString(),
                    title + "icon.png"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ""
            }
            ps2Games.add(Game(title, icon, link, info))
        }
        return ps2Games
    }

    @Throws(Exception::class)
    private fun getPSPGames(document: Document): List<Game> {
        val games = document.getElementById("seg_wm_psp_items")
        val tables = games.getElementsByTag("Table")
        val ps2Games: MutableList<Game> = ArrayList<Game>()
        for (table in tables) {
            if (table.attr("key") == "ps2_classic_launcher") continue
            val singleton = table.children()
            var title = ""
            var icon = ""
            var link = ""
            var info = ""
            for (s in singleton) {
                if (s.attr("key") == "title") {
                    title = s.text()
                } else if (s.attr("key") == "icon") {
                    icon = s.text()
                } else if (s.attr("key") == "module_action") {
                    link = s.text().replace("127.0.0.1", service.targetIp.toString())
                } else if (s.attr("key") == "info") {
                    info = s.text()
                }
            }
            icon = try {
                downloadFile(
                    "http://" + service.targetIp.toString() + ":80" + icon.replace(" ", "%20"),
                    Environment.getExternalStorageDirectory().toString(),
                    title + "icon.png"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ""
            }
            ps2Games.add(Game(title, icon, link, info))
        }
        return ps2Games
    }

    @Throws(Exception::class)
    private fun getPS2Games(document: Document): List<Game> {
        val ps2_games = document.getElementById("seg_wm_ps2_items")
        val ps2_tables = ps2_games.getElementsByTag("Table")
        val ps2Games: MutableList<Game> = ArrayList<Game>()
        for (table in ps2_tables) {
            if (table.attr("key") == "ps2_classic_launcher") continue
            val singleton = table.children()
            var title = ""
            var icon = ""
            var link = ""
            var info = ""
            for (s in singleton) {
                when {
                    s.attr("key") == "title" -> {
                        title = s.text()
                    }
                    s.attr("key") == "icon" -> {
                        icon = s.text()
                    }
                    s.attr("key") == "module_action" -> {
                        link = s.text().replace("127.0.0.1", service.targetIp.toString())
                    }
                    s.attr("key") == "info" -> {
                        info = s.text()
                    }
                }
            }
            icon = try {
                downloadFile(
                    "http://" + service.targetIp.toString() + ":80" + icon.replace(" ", "%20"),
                    Environment.getExternalStorageDirectory().toString(),
                    title + "icon.png"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ""
            }
            ps2Games.add(Game(title, icon, link, info))
        }
        return ps2Games
    }

    @Throws(Exception::class)
    private fun getPS3Games(document: Document): List<Game> {
        val ps3_game = document.getElementById("seg_wm_ps3_items")
        val ps3_tables = ps3_game.getElementsByTag("Table")
        val ps3Games: MutableList<Game> = ArrayList<Game>()
        for (table in ps3_tables) {
            val singleton = table.children()
            var title = ""
            var icon = ""
            var link = ""
            var info = ""
            for (s in singleton) {
                when {
                    s.attr("key") == "title" -> {
                        title = s.text()
                    }
                    s.attr("key") == "icon" -> {
                        icon = s.text()
                    }
                    s.attr("key") == "module_action" -> {
                        link = s.text().replace("127.0.0.1", service.targetIp.toString())
                    }
                    s.attr("key") == "info" -> {
                        info = s.text()
                    }
                }
            }
            try {
                icon = downloadFile(
                    "http://" + service.targetIp.toString() + ":80" + icon.replace(
                        " ",
                        "%20"
                    ), Environment.getExternalStorageDirectory().toString(), title + "icon.png"
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            ps3Games.add(Game(title, icon, link, info))
        }
        return ps3Games
    }

    @Throws(Exception::class)
    private fun downloadFile(link: String, dest: String, filename: String): String {
        val f = File("$dest/WebMan/")
        if (!f.exists()) {
            f.mkdirs()
        }
        if (File("$dest/WebMan/", filename).exists()) {
            Log.e("File Exist", "The File Exist")
            return "$dest/WebMan/$filename"
        }
        val fis = FileOutputStream("$dest/WebMan/$filename")
        val stream = download(link)?.byteStream()
        val copied = stream?.copyTo(fis)
        fis.flush()
        fis.close()
        return "$dest/WebMan/$filename"
    }

    private fun download(ip: String): ResponseBody? {
        return getRequest(ip).body
    }

    @Throws(SocketException::class)
    private fun getWLANipAddress(protocolVersion: String): InetAddress? {
        val nets = NetworkInterface.getNetworkInterfaces()
        for (netint in Collections.list(nets)) {
            if (netint.isUp && !netint.isLoopback && !netint.isVirtual) {
                val inetAddresses = netint.inetAddresses
                for (inetAddress in Collections.list(inetAddresses)) {
                    if (protocolVersion == "IPv4") {
                        if (inetAddress is Inet4Address) {
                            return inetAddress
                        }
                    } else {
                        if (inetAddress is Inet6Address) {
                            return inetAddress
                        }
                    }
                }
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun exists(url: String): Boolean {
        val response = getRequest(url)
        val s = response.body?.string().toString()
        Log.e("CONTENT", s.toString())
        return s.lowercase().contains("ps3mapi") || s.lowercase()
            .contains("webman") || s.lowercase().contains("dex") ||
                s.lowercase().contains("d-rex") || s.lowercase().contains("cex") || s.lowercase()
            .contains("rebug") ||
                s.lowercase().contains("rsx")
    }

    override fun notify(message: String) {
        val replace = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        val url = "http://${service.targetIp}:80/popup.ps3/$replace";
        download(url)
    }

    fun refresh() {
        val url = ("http://${service.targetIp}:80/refresh.ps3")
        download(url)
    }

    fun insert() {
        val url = ("http://${service.targetIp}:80/insert.ps3")
        download(url)
    }

    fun eject() {
        val url = ("http://${service.targetIp}:80/eject.ps3")
        download(url)
    }

    fun unmount() {
        val url = ("http://${service.targetIp}:80/mount.ps3/unmount")
        download(url)
    }

    override fun boot(ps3boot: Boot) {
        val ip = service.targetIp
        val url = when (ps3boot) {
            Boot.REBOOT -> "http://$ip:80/restart.ps3"
            Boot.SOFTREBOOT -> "http://$ip:80/restart.ps3"
            Boot.HARDREBOOT -> "http://$ip:80/restart.ps3"
            Boot.SHUTDOWN -> "http://$ip:80/shutdown.ps3"
        }
        getRequest(url)
    }

    @Throws(SocketException::class)
    fun verify(): Boolean {
        return exists("http://${service.targetIp}:80/index.ps3")
    }

}