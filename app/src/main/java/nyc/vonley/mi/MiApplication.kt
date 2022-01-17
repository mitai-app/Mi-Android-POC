package nyc.vonley.mi

import android.app.Application
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.MiJBServer
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.intents.PSXService
import javax.inject.Inject

@HiltAndroidApp
class MiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, PSXService::class.java))
    }

}