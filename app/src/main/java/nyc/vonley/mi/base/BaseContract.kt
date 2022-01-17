package nyc.vonley.mi.base

import kotlinx.coroutines.CoroutineScope
import nyc.vonley.mi.di.network.listeners.OnClientListener
import nyc.vonley.mi.models.Console

interface BaseContract {

    interface View {
        fun onError(e: Throwable)
    }

    interface Presenter : CoroutineScope, OnClientListener {
        fun init()
        fun cleanup()
    }

}