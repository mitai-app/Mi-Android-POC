package nyc.vonley.mi.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.Features
import java.net.InetAddress

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

fun InetAddress.client(): Client {
    return Client(this)
}

fun Client.console(): Console? {
    val actives = activePorts
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
        return Console(hostName, hostName, type, features)
    }
    return null
}