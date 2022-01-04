package nyc.vonley.mi.di.network.handlers.base

import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import nyc.vonley.mi.di.network.handlers.ClientHandler
import nyc.vonley.mi.di.network.listeners.OnClientListener
import kotlin.coroutines.CoroutineContext

abstract class BaseClientHandler <N : OnClientListener, D> : CoroutineScope, Observer<D>,
    ClientHandler {

    private val job = Job()

    val listeners = HashMap<Class<*>, N>()

    override fun onChanged(t: D?) {
        if (t != null) {
            handle(t)
        }
    }

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    protected abstract fun handle(event: D): Job

}