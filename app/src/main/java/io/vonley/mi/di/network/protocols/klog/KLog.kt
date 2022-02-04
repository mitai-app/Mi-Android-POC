package io.vonley.mi.di.network.protocols.klog

import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

interface KLog : PSXProtocol {

    override val feature: Feature get() = Feature.KLOG
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket get() = _socket!!

    interface KLogger {
        fun onLog(string: String)
    }

    val logger: KLogger

    var onGoing: Job?

    fun connect() {
        if(onGoing?.isActive == true) return
        if(onGoing?.isCompleted == true || onGoing?.isCancelled == true|| onGoing == null) {
            when (_socket?.isConnected) {
                true -> {
                    onGoing = launch {
                        val br = socket.getInputStream().bufferedReader()
                        var stub: String? = null
                        while (socket.isConnected) {
                            while (br.readLine()?.let { stub = it } != null) {
                                withContext(Dispatchers.Main) {
                                    stub?.let { logger.onLog(it) }
                                }
                            }
                        }
                    }
                }
                else -> try {
                    val sockAddr = InetSocketAddress(service.targetIp, feature.ports.first())
                    socket.connect(sockAddr, 2000)
                    connect()
                } catch (e: Throwable) {

                }
            }
        }
    }
}