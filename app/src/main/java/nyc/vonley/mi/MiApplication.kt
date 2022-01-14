package nyc.vonley.mi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.MiJBServer
import nyc.vonley.mi.di.network.SyncService
import javax.inject.Inject

@HiltAndroidApp
class MiApplication : Application() {

    @Inject
    lateinit var sync: SyncService

    @Inject
    lateinit var jb: MiJBServer

    override fun onCreate() {
        super.onCreate()
        sync.getClients()
        jb.init(this)
    }

}