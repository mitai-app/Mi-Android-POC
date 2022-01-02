package nyc.vonley.mi.models

import android.util.Log
import nyc.vonley.mi.models.enums.Features
import okhttp3.internal.closeQuietly
import java.net.InetAddress
import java.net.Socket

data class Client(
    val address: InetAddress
) {
    val reachable: Boolean
        get() = address.isReachable(50)

    val hostName: String
        get() = address.canonicalHostName

    val activePorts: List<Int>
        get() {
            val ports = Features.values().map { f -> f.port }.filter { f -> f > 0 }.toTypedArray()

            val result = ports.map { port ->
                try {
                    Log.e("CLIENT:NON_ACTIVE_PORT", "$port is not active")
                    val socket = Socket(address, port)
                    if (socket.isConnected) {
                        Log.e("CLIENT:ACTIVE_PORT", "$port is active")
                        socket.closeQuietly()
                        return@map port
                    }
                    socket.closeQuietly()
                }catch (e: Throwable){
                    Log.e("ERROR", e.message?:"Something went wrong")
                }
                return@map 0
            }.filter { p -> p != 0 }
            return result
        }
}