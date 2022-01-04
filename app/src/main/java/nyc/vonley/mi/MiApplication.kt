package nyc.vonley.mi

import android.app.Application
<<<<<<< HEAD
import dagger.hilt.android.HiltAndroidApp
=======
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import nyc.vonley.mi.di.network.ClientSync
import javax.inject.Inject
>>>>>>> b6ad848beeff89f262b87d4d684f9a420852a922

@HiltAndroidApp
class MiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}