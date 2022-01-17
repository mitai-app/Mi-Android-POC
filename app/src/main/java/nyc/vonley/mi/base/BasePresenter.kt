package nyc.vonley.mi.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter : BaseContract.Presenter {

    protected val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

}