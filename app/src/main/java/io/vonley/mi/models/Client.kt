package io.vonley.mi.models

import android.util.Log
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.network.impl.SyncServiceImpl
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.impl.set
import io.vonley.mi.extensions.e
import io.vonley.mi.extensions.i
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.models.enums.Protocol
import okhttp3.Request
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

    /**
     * Check for active ports. while avoiding unstable ports
     * Stored allowed ports into
     * @see SyncServiceImpl.cachedTargets
     */
    fun openActivePorts(service: SyncService): List<Feature> {
        val features = Feature.stableFeatures
        val allowed = Feature.allowedToOpen
        val result = features.mapNotNull features@{ feature ->
            if (feature in allowed && feature.protocol == Protocol.SOCKET) {
                try {
                    service[this] = feature
                    if (service[this, feature] != null) {
                        return@features feature
                    }
                } catch (e: Throwable) {
                    "$ip does not have $feature".e("Client:FailToConnect")
                }
                return@features Feature.NONE
            } else {
                val map = feature.ports.toList().map port@{ port ->
                    try {
                        //TODO Fix for webman and ccapi
                        return@port when (feature) {
                            Feature.WEBMAN -> if(feature.validate(this, service)) feature else Feature.NONE
                            else -> { //Feature.CCAPI should validate via http too
                                val socket = Socket()
                                val socketAddress = InetSocketAddress(ip, port)
                                socket.connect(socketAddress, 1000)
                                if (socket.isConnected) {
                                    "${ip}:{$port} aka $feature is active".i("Client:Connected")
                                    socket.close()
                                    return@port feature
                                }
                                return@port Feature.NONE
                            }
                        }
                    } catch (e: Throwable) {
                        "$ip does not have $feature".e("Client:FailToConnect")
                    }
                    return@port Feature.NONE
                }.distinct().firstOrNull { p -> p != Feature.NONE }
                return@features map
            }
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

val List<Feature>.isPs3
    get() = this.any { p -> p  in PlatformType.PS3.features }

val List<Feature>.isPs4
    get() = this.any { p -> p  in PlatformType.PS4.features }
