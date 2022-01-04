package nyc.vonley.mi.intents

import android.os.Binder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import nyc.vonley.mi.di.network.impl.ClientSyncService
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PSXServiceBinder @Inject constructor(val clientSync: ClientSyncService) : Binder(), CoroutineScope {
    protected val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job




    companion object {

        val TAG = PSXServiceBinder::class.java.name
    }
}