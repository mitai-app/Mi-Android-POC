package io.vonley.mi.di.network.protocols.webman


import android.os.Environment
import android.util.Log
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
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
import kotlin.reflect.KClass

data class Game(var title: String, var icon: String, var link: String, val info: String)


enum class GameType {
    PS3, PSP, PS2, PSX, QUERY
}

/**
 * Need Parse Settings App with jsoup,
 * Then decode these params into readable human text
 * this will be fun...
 */

enum class Setup(
    val title: String,
    val param: String,
    value: KClass<*>,
    vararg coupled: CoupledSetup
) {
    //region Scan these devices
    USB0("USB000", "u0", Boolean::class),
    USB1("USB001", "u1", Boolean::class),
    USB2("USB002", "u2", Boolean::class),
    USB3("USB003", "u3", Boolean::class),
    USB6("USB006", "u6", Boolean::class),
    USB7("USB007", "u7", Boolean::class),

    //endregion
    //region Scan for content
    PS3("Playstation 3", "ps3", Boolean::class),
    PS2("Playstation 2", "ps2", Boolean::class),
    P2L("Show PS2 Classic Launcher", "p2l", Boolean::class),
    PS1("Playstation 1", "ps1", Boolean::class),
    PSE("PS1 Net Emu", "pse", Boolean::class),
    PSP("Playstation Portable", "psp", Boolean::class),
    PSL("Show PSP Launcher", "psl", Boolean::class),
    BLU("Blu-ray", "blu", Boolean::class),
    RXV("Show Video Folder", "rxv", Boolean::class),
    DVD("DVD Video", "dvd", Boolean::class),

    //endregion
    LastPlayed("Load last-played game on startup", "lp", Boolean::class), //0,1
    EnableOnStartup(
        "Check for %s on startup",
        "ab",
        Boolean::class,
        CoupledSetup.CheckForOnStartup
    ), // 0,1
    DelayLoadingStartup("Delay loading of %s/last-game (Disc auto-start)", "dy", Boolean::class),
    EnableDevBlind("Enable /dev_blind (writable /dev_flash) on startup", "bl", Boolean::class),
    DisableStartupNotification(
        "Disable startup notification of WebMAN on the XMB",
        "wn",
        String::class
    ), //wmdn?
    DisableContentScanOnStartup("Disable content scan on startup", "rf", String::class), //refr
    DisableUSBPolling("Disable USB polling", "pl", Boolean::class),
    DisableFTP("Disable FTP Service", "ft", Boolean::class),
    DisableAllPadShortcuts("Disable All Pad Shortcuts", "np", Boolean::class),
    DisableRemoteAccessToFTPWWWServices(
        "Disable Remote access to FTP & WWW Services",
        "ip",
        Boolean::class,
        CoupledSetup.DisableRemoteAccessToFTPWWWServices
    ),
    DisableFirmwareSpoofing("Disable firmware spoofing", "nsp", Boolean::class),

    DisableGroupOfContentInWebmanGames(
        "Disable grouping of content in Webman games",
        "ng",
        Boolean::class
    ),
    DisableWebmanSetupEntryInWebmanGames(
        "Disable Webman setup entry in Webman games",
        "ns",
        Boolean::class
    ),
    DisableWebmanCovers("Disable Webman covers", "nc", Boolean::class),
    IncludeTheIdAsPartOfTheTitleOfTheGame("Disable Webman covers", "tid", Boolean::class),
    DisableResetUSBBus("Disable reset USB bus", "bus", Boolean::class),

    EnableDynamicFanControl("Enable Dynamic Fan Control", "fc", Boolean::class),
    DisableTemperatureWarnings("Disable Temperature Warnings", "warn", Boolean::class),
    SetFanMode(
        "Dynamic fan control at: %s C",
        "temp",
        Boolean::class,
        CoupledSetup.SetFanDegree, CoupledSetup.SetFanSpeed
    ), //temp is 0 - Automatic, 1 - Manual
    LowestFanSpeed("Lowest fan speed", "mfam", Int::class), //Lowest Fan Speed
    PS2EmulatorFanSpeed("PS2 emulator fan speed", "fsp0", Int::class),
    ShortcutFailSafe("Fail Safe: SELECT+L3+L2+R2", "pfs", Boolean::class),
    ShortcutShowTemp("Show Temp: SELECT+START", "pss", Boolean::class),
    ShortcutPrevGame("Show Temp: SELECT+START", "ppv", Boolean::class),
    ShortcutNextGame("Show Temp: SELECT+START", "pnx", Boolean::class),
    ShortcutUnloadWM("", "puw", Boolean::class),
    ShortcutCtrlFan("", "pf1", Boolean::class),
    ShortcutCtrlDynFan("Show Temp: SELECT+START", "pdf", Boolean::class),
    ShortcutCtrlMinFan("Show Temp: SELECT+START", "pf2", Boolean::class),
    ShortcutUnmount("Show Temp: SELECT+START", "umt", Boolean::class),
    ShortcutDelCFWSysCalls("Show Temp: SELECT+START", "psc", Boolean::class),
    ShortcutOffline("Show Temp: SELECT+START", "psv", Boolean::class),
    ShortcutCobraToggle("Show Temp: SELECT+START", "pdc", Boolean::class),
    ShortcutGameData("Show Temp: SELECT+START", "pgd", Boolean::class),
    ShortcutNet0("Show Temp: SELECT+START", "pn0", Boolean::class),
    ShortcutNet1("Show Temp: SELECT+START", "pn1", Boolean::class),
    ShortcutPS2Classic("Show Temp: SELECT+START", "p2c", Boolean::class),
    ShortcutPS2Switch("Show Temp: SELECT+START", "p2s", Boolean::class),
    ShortcutRefreshXML("Show Temp: SELECT+START", "pxr", Boolean::class),
    ShortcutShowIDPS("Show Temp: SELECT+START", "pid", Boolean::class),
    ShortcutShutdown("Show Temp: SELECT+START", "psd", Boolean::class),
    ShortcutRestart("Show Temp: SELECT+START", "prs", Boolean::class),

    ID1("IDPS", "id1", Boolean::class, *arrayOf(CoupledSetup.IDPS1, CoupledSetup.IDPS2)),
    ID2("PSID", "id2", Boolean::class, *arrayOf(CoupledSetup.PSID1, CoupledSetup.PSID2)),
    /**
     *
    neth0: 127.0.0.1
    netp0: 38008
    neth1:
    netp1: 38008
    neth2:
    netp2: 38008
    b: 0
    s: 3
    hurl:
    usr: 0
    uacc: 00000001
    fp: 1
    l: 0
     */
}

enum class CoupledSetup(title: String, param: String, kClass: KClass<*>) {
    CheckForOnStartup("Check for %s on startup", "autop", String::class), //text
    DisableRemoteAccessToFTPWWWServices(
        "Disable remote access to ftp/www services",
        "aip",
        String::class
    ),
    SetFanDegree("Set fan degree", "step", Int::class), // For Automatic
    SetFanSpeed("Set fan speed", "manu", Int::class), // For Manual

    IDPS1("Set IDPS 0-16", "vID1", String::class),
    IDPS2("Set IDPS 16-32", "vID2", String::class),

    PSID1("Set PSID 0-16", "vPS1", String::class),
    PSID2("Set PSID 16-32", "vPS2", String::class),
}


interface WebMan : PSXProtocol {

    override val feature: Feature get() = Feature.WEBMAN
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    val TAG: String get() = WebMan::class.java.name


    private val gameUrl: String
        get() = "http://${service.targetIp}/dev_hdd0/xmlhost/game_plugin/mygames.xml"

    @Throws(IOException::class)
    suspend fun searchGames(): Map<GameType, List<Game>> {
        val xml: String = gameUrl
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
    suspend fun getPSXGames(document: Document): List<Game> {
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
    suspend fun getPSPGames(document: Document): List<Game> {
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
    suspend fun getPS2Games(document: Document): List<Game> {
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
    suspend fun getPS3Games(document: Document): List<Game> {
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

    override suspend fun notify(message: String) {
        val replace = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        val url = "http://${service.targetIp}:80/popup.ps3/$replace";
        download(url)
    }

    override suspend fun buzzer(buzz: Buzzer) {
        val ip = service.targetIp
        val ordinal = when (buzz) {
            Buzzer.CONTINUOUS -> Buzzer.TRIPLE.ordinal
            else -> buzz.ordinal
        }
        val url = "http://$ip:80/buzzer.ps3mapi?mode=${ordinal}"
        getRequest(url)
    }


    override suspend fun boot(ps3boot: Boot) {
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

    override suspend fun refresh() {
        val url = ("http://${service.targetIp}:80/refresh.ps3")
        download(url)
    }

    override suspend fun insert() {
        val url = ("http://${service.targetIp}:80/insert.ps3")
        download(url)
    }

    override suspend fun eject() {
        val url = ("http://${service.targetIp}:80/eject.ps3")
        download(url)
    }

    override suspend fun unmount() {
        val url = ("http://${service.targetIp}:80/mount.ps3/unmount")
        download(url)
    }


}