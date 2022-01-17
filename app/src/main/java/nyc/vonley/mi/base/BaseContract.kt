package nyc.vonley.mi.base

import kotlinx.coroutines.CoroutineScope
import nyc.vonley.mi.di.network.listeners.OnClientListener

interface BaseContract {

    interface View {
        fun onError(e: Throwable)
    }

    interface Presenter : CoroutineScope, OnClientListener {
        fun init()
        fun cleanup()
    }

}