package io.vonley.mi.models

import android.util.Log
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.impl.set
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

interface Client {

    val ip: String
    var name: String
    var type: PlatformType
    var features: List<Feature>
    var wifi: String
    var lastKnownReachable: Boolean
    var pinned: Boolean

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

    fun getActivePorts(service: SyncService): List<Feature> {
        val features =
            Feature.values().filter { f -> f != Feature.NETCAT && f != Feature.GOLDENHEN }
                //.map { f -> f.ports }
                //.flatMap { it.iterator().asSequence() }
                .filter { f -> f.ports.first() > 0 }.toTypedArray()

        val allowed = arrayOf(Feature.CCAPI, Feature.WEBMAN, Feature.PS3MAPI)
        val result = features.mapNotNull features@{ feature ->
            try {
                if (feature in allowed) {
                    service[this] = feature
                    if (service[this, feature] != null) {
                        return@features feature
                    } else return@features Feature.NONE
                } else {
                    val map = feature.ports.toList().mapNotNull port@{ port ->
                        try {
                            Log.i("[Client:CheckPort]", "Checking ${ip}:$port")
                            val socket = Socket()
                            val socketAddress = InetSocketAddress(ip, port)
                            socket.connect(socketAddress, 1000)
                            if (socket.isConnected) {
                                Log.i("Client:Connected", "${ip}:$feature is active")
                                socket.close()
                                return@port feature
                            }
                        } catch (e: Throwable) {
                            Log.e("[Client:FailToConnect]", "${ip}:$feature ")
                        }
                        return@port Feature.NONE
                    }.distinct().firstOrNull()
                    return@features map
                }
            } catch (e: Throwable) {
                Log.e("[Client:FailToConnect]", "${ip}:$feature ")
            }
            return@features Feature.NONE
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