package nyc.vonley.mi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.ClientSync
import javax.inject.Inject

@HiltAndroidApp
class MiApplication : Application() {

    @Inject
    lateinit var sync: ClientSync

    override fun onCreate() {
        super.onCreate()
        sync.getClients()
    }

}