package io.vonley.mi.di.network.protocols.common.models

import io.vonley.mi.di.network.protocols.ccapi.models.ConsoleType
import io.vonley.mi.di.network.protocols.ps3mapi.models.Temperature

data class ConsoleInfo constructor(
    var firmware: String,
    val type: ConsoleType,
    val temp: Temperature
)