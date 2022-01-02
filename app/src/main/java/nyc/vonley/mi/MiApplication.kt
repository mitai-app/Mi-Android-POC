package nyc.vonley.mi

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.ClientSync
import javax.inject.Inject

@HiltAndroidApp
class MiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}