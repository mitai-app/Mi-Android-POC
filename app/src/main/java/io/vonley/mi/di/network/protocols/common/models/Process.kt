package io.vonley.mi.di.network.protocols.common.models

data class Process(val name: String, val process: String) {
    companion object {
        fun create(name: String, process: String) = Process(name, process)
    }
}