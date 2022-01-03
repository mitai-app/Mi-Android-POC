package nyc.vonley.mi.base

import kotlinx.coroutines.CoroutineScope
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.Console

interface BaseContract {

    interface View {
        fun onError(e: Throwable)
        fun onConsoleFound(console: Console)
    }

    interface Presenter : CoroutineScope, ClientSync.ConsoleListener {
        fun init()
        fun cleanup()
    }

}