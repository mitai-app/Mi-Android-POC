package nyc.vonley.mi.models

import android.util.Log
import nyc.vonley.mi.models.enums.Features
import okhttp3.internal.closeQuietly
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class Client(
    val address: InetAddress
) {
    var lastKnownReachable: Boolean = false

    val reachable: Boolean
        get() {
            return try {
                lastKnownReachable = address.isReachable(100)
                lastKnownReachable
            }catch (throwable: Throwable){
                false
            }
        }

    val hostName: String
        get() = address.canonicalHostName

    fun getActivePorts(): List<Int> {
        val ports = Features.values().map { f -> f.port }.filter { f -> f > 0 }.toTypedArray()
        val result = ports.map { port ->
            try {
                Log.i("[Client:CheckPort]", "Checking $hostName:$port")
                val socket = Socket()
                val socketAddress = InetSocketAddress(address, port)
                socket.connect(socketAddress, 500)
                if (socket.isConnected) {
                    Log.i("Client:Connected", "$hostName:$port is active")
                    socket.closeQuietly()
                    return@map port
                }
            } catch (e: Throwable) {
                Log.e("[Client:FailedToConnect]", "$hostName:$port ")
            }
            return@map Features.NONE.port
        }.distinct()
        return result
    }


    override fun toString(): String {
        return """
                    HostName: $hostName
                    Reachable: $lastKnownReachable
                """.trimIndent()
    }
}