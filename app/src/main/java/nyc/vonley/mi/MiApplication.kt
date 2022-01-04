package nyc.vonley.mi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}