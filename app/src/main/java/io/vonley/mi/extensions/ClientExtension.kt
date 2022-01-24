package io.vonley.mi.extensions

import android.net.wifi.WifiInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.vonley.mi.models.Client
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.ConsoleType
import io.vonley.mi.models.enums.Feature
import java.net.InetAddress

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> String.fromJson(): T? =
    GsonBuilder().create().fromJson<T>(this, object : TypeToken<T>() {}.type)

inline fun <reified T> T.toJson(): String =
    GsonBuilder().create().toJson(this)

fun InetAddress.client(wi: WifiInfo): Client {
    return object : Client {

        private var deviceName: String = canonicalHostName
        private var isReachable = false
        private var consoleType: ConsoleType = ConsoleType.UNKNOWN
        private var wifiInfo: String = wi.ssid ?: "not connected?"
        private var feats: List<Feature> = emptyList()
        private var pin: Boolean = false


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

        override var features: List<Feature>
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
        override var pinned: Boolean
            get() = pin
            set(value) {
                pin = value
            }

    }
}

fun Client.console(): Console? {
    val actives = getActivePorts()
    if (actives.isNotEmpty()) {
        val features = actives.map { port ->
            val values = Feature.values().filter { f -> f != Feature.NETCAT && f != Feature.GOLDENHEN }
            val value = values.find { f -> f.ports.find { p -> p == port } == port }
            return@map if (value != null) Feature.valueOf(value.name) else Feature.NONE
        }
        // TODO: For now we only recognize goldenhen ports, when stable it should perform
        val type = if (features.contains(Feature.GOLDENHEN)) {
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