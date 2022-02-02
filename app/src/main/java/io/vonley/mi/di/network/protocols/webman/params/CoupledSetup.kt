package io.vonley.mi.di.network.protocols.webman.params

import kotlin.reflect.KClass

enum class CoupledSetup(title: String, param: String, kClass: KClass<*>) {
    CheckForOnStartup("Check for %s on startup", "autop", String::class), //text
    DisableRemoteAccessToFTPWWWServices(
        "Disable remote access to ftp/www services",
        "aip",
        String::class
    ),
    SetFanDegree("Set fan degree", "step", Int::class), // For Automatic
    SetFanSpeed("Set fan speed", "manu", Int::class), // For Manual

    IDPS1("Set IDPS 0-16", "vID1", String::class),
    IDPS2("Set IDPS 16-32", "vID2", String::class),

    PSID1("Set PSID 0-16", "vPS1", String::class),
    PSID2("Set PSID 16-32", "vPS2", String::class),
}