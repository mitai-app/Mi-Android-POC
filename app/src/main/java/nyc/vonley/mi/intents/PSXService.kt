package nyc.vonley.mi.intents

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.R
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.ui.main.MainActivity
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

    override fun onCreate() {
        super.onCreate()

    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        if (manager.jbService) {
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