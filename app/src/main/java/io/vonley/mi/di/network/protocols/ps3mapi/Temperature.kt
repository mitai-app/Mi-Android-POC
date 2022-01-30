package io.vonley.mi.di.network.protocols.ps3mapi


data class Temperature protected constructor(val cpu: String, val rsx: String) {

    val cpuF: String get() = (cpu.toInt() * 9 / 5 + 32).toString()
    val rsxF: String get() = (rsx.toInt() * 9 / 5 + 32).toString()

    companion object {
        fun instantiate(cpu: String, rsx: String): Temperature = Temperature(cpu, rsx)

    }
}
