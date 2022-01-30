package io.vonley.mi.di.network.protocols.common.models

data class ConsoleInfo constructor(
    val firmware: String,
    val type: ConsoleType,
    val temp: Temperature
)