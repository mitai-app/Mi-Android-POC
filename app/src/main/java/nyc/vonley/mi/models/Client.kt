package nyc.vonley.mi.models

import android.util.Log
import okhttp3.internal.closeQuietly
import java.net.InetAddress
import java.net.Socket

data class Client(
    val address: InetAddress
) {
    val reachable: Boolean
        get() = address.isReachable(200)

    val hostName: String
        get() = address.canonicalHostName

    val activePorts: List<Int>
        get() {
            val ports = arrayOf(9090, 2121)
            val result = ports.map { port ->
                try {
                    val socket = Socket(address, port)
                    if (socket.isConnected) {
                        Log.e("CLIENT:ACTIVE_PORT", "$port is active")
                        socket.closeQuietly()
                        return@map port
                    }
                    socket.closeQuietly()
                }catch (e: Throwable){
                    //Log.e("ERROR", e.message?:"Something went wrong")
                }
                return@map 0
            }.filter { p -> p != 0 }
            return result
        }
}