package nyc.vonley.mi

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.ClientSync

@HiltAndroidApp
class MiApplication : Application() {

    lateinit var sync: ClientSync

    override fun onCreate() {
        super.onCreate()
        sync = ClientSync(this)
        sync.getClients { clients ->
            clients.forEach { client ->
                Log.e("Client", client.hostName)
            }
        }
    }

}