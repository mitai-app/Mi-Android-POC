package nyc.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VoiceService : Service() {

    @Inject
    lateinit var voidServiceBinder: VoiceServiceBinder

    override fun onBind(intent: Intent): IBinder {
        Log.e("SERVICE", "CREATED")
        return voidServiceBinder
    }

}