package nyc.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PSXService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}