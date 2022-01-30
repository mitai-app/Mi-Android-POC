package io.vonley.mi.di.network.protocols

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.models.enums.Feature
import java.net.Socket

interface PSXFunction {
    fun notify(message: String)
}

interface PSXProtocol: PSXFunction {
    val service: PSXService
    val feature: Feature
    val socket: Socket?
}