package io.vonley.mi.di.network.protocols

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.models.enums.Feature
import java.net.Socket

class PS3MAPIProtocol(
    override val service: PSXService,
) : PSXProtocol {

    override val feature: Feature = Feature.PS3MAPI
    private val _socket: Socket? get() = service[service.target!!, feature]
    override val socket: Socket? get() = _socket!!

    override fun notify(message: String) {
        val first = feature.ports.first()
    }

}