package io.vonley.mi.di.network.protocols.goldenhen

import io.vonley.mi.di.network.PSXBin
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.models.enums.Feature
import java.net.Socket

interface GoldenHen: PSXProtocol {

    override val feature: Feature get() = Feature.GOLDENHEN
    private val _socket: Socket? get() = service[service.target, feature]
    override val socket: Socket get() = _socket!!

    override val TAG: String
        get() = KLog::class.qualifiedName ?: KLog::javaClass.name


}