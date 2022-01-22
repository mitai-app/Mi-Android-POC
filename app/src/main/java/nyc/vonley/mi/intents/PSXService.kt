package nyc.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class PSXService : Service() {

    val TAG = PSXService::class.java.name

    @Inject
    lateinit var binder: PSXServiceBinder

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        if(manager.jbService){
            binder.jb.startService()
        }
        binder.sync.getClients(true)
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        binder.sync.stop()
        binder.jb.stopService()
        super.onDestroy()
    }


}