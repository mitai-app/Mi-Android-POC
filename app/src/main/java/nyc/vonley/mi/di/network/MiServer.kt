package nyc.vonley.mi.di.network

import nyc.vonley.mi.di.network.impl.MiServerImpl

interface MiServer {
    val activePort: Int
    val service: PSXService
    val sync: SyncService get() = service.sync
    fun stopService()
    fun startService()
    fun restartService() {
        stopService()
        startService()
    }
    fun add(jb: MiServerImpl.MiJbServerListener)
    fun remove(jb: MiServerImpl.MiJbServerListener)
}