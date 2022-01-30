package io.vonley.mi.di.network.protocols.ccapi.models

enum class ConsoleType {
    UNK, CEX, DEX, TOOL;

    companion object {
        fun parse(type: String): ConsoleType {
            return values().find { p -> p.name.lowercase() == type.lowercase() }?: UNK
        }
    }
}