package nyc.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.di.network.MiJBServer
import nyc.vonley.mi.di.network.SyncService
import javax.inject.Inject

@AndroidEntryPoint
class PSXService : Service() {

    val TAG = PSXService::class.java.name

    @Inject
    lateinit var binder: PSXServiceBinder

    @Inject
    lateinit var jb: MiJBServer

    @Inject
    lateinit var sync: SyncService

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        jb.init(this)
        sync.getClients(true, 15)
        return START_STICKY
    }


    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onDestroy() {
        sync.stop()
        jb.stopService()
        super.onDestroy()
    }


}