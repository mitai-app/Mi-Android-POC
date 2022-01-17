package nyc.vonley.mi.models

import android.util.Log
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.Feature
import okhttp3.internal.closeQuietly
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

interface Client {

    val ip: String
    var name: String
    var type: ConsoleType
    var features: List<Feature>
    var wifi: String
    var lastKnownReachable: Boolean

    fun getInetAddress(): InetAddress? {
        return try {
            InetAddress.getByName(ip)
        } catch (e: Throwable) {
            return null
        }
    }

    fun getReachable(): Boolean {
        return try {
            val address = getInetAddress()
            if (address != null) {
                lastKnownReachable = address.isReachable(100)
                lastKnownReachable
            }
            false
        } catch (throwable: Throwable) {
            false
        }
    }

    fun getActivePorts(): List<Int> {
        val ports = Feature.values().filter { f -> f != Feature.NETCAT }.map { f -> f.ports }.flatMap { it.iterator().asSequence() }
            .filter { f -> f > 0 }.toTypedArray()
        val result = ports.map { port ->
            try {
                Log.i("[Client:CheckPort]", "Checking ${ip}:$port")
                val socket = Socket()
                val socketAddress = InetSocketAddress(ip, port)
                socket.connect(socketAddress, 1000)
                if (socket.isConnected) {
                    Log.i("Client:Connected", "${ip}:$port is active")
                    socket.closeQuietly()
                    return@map port
                }
            } catch (e: Throwable) {
                Log.e("[Client:FailedToConnect]", "${ip}:$port ")
            }
            return@map 0
        }.distinct()
        return result
    }

    val debug: String
        get() {
            return """
                    HostName: $ip
                    Reachable: $lastKnownReachable
                """.trimIndent()
        }
}

val Client.activeFeatures
    get() = features.filter { p -> p != Feature.NONE }
val Client.featureString
    get() = activeFeatures.joinToString { f -> f.title }