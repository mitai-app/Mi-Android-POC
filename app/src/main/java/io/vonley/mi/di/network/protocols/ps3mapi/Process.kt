package io.vonley.mi.di.network.protocols.ps3mapi

data class Process(val title: String, val process: String) {
    companion object {
        fun create(title: String, process: String) = Process(title, process)
    }
}