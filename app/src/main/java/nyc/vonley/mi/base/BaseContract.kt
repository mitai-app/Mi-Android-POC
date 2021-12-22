package nyc.vonley.mi.base

import kotlinx.coroutines.CoroutineScope

interface BaseContract {

    interface View {
        fun onError(e: Throwable)
    }

    interface Presenter : CoroutineScope {
        fun cleanup()
    }

}