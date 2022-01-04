package nyc.vonley.mi.extensions

import android.net.wifi.WifiInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.Features
import java.net.InetAddress

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

fun InetAddress.client(wi: WifiInfo): Client {
    return object : Client {

        private var deviceName: String = canonicalHostName
        private var isReachable = false
        private var consoleType: ConsoleType = ConsoleType.UNKNOWN
        private var wifiInfo: String = wi.ssid ?: "not connected?"
        private var feats: List<Features> = emptyList()


        override val ip: String
            get() = hostAddress ?: canonicalHostName

        override var name: String
            get() = deviceName
            set(value) {
                deviceName = value
            }

        override var type: ConsoleType
            get() = consoleType
            set(value) {
                consoleType = value
            }

        override var features: List<Features>
            get() = feats
            set(value) {
                feats = value
            }

        override var wifi: String
            get() = wifiInfo
            set(value) {
                wifiInfo = value
            }


        override var lastKnownReachable: Boolean
            get() = isReachable
            set(value) {
                isReachable = value
            }

    }
}

fun Client.console(): Console? {
    val actives = getActivePorts()
    if (actives.isNotEmpty()) {
        val features = actives.map { port ->
            val values = Features.values()
            val value = values.find { f -> f.port == port }
            return@map if (value != null) Features.valueOf(value.name) else Features.NONE
        }
        val type = if (features.contains(Features.GOLDENHEN)) {
            ConsoleType.PS4
        } else {
            ConsoleType.UNKNOWN
        }
        return Console(
            ip,
            ip,
            type,
            features,
            lastKnownReachable,
            wifi
        )
    }
    return null
}