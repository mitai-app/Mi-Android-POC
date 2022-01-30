package io.vonley.mi.di.network.protocols.common.cmds

enum class Boot(val code: Int) {
    SHUTDOWN(1), SOFTREBOOT(2), REBOOT(3), HARDREBOOT(3);

}