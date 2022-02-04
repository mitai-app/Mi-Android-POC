package io.vonley.mi.di.network.protocols.klog

import android.text.Spannable
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.ui.main.console.sheets.adapters.KLoggingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class KLogImpl @Inject constructor(override val service: PSXService) : KLog, KLog.KLogger {

    private val job = Job()

    override val loggers: HashMap<Class<*>, KLog.KLogger> = hashMapOf(Pair(this.javaClass, this))

    override var onGoing: Job? = null

    override fun attach(logger: KLog.KLogger) {
        loggers[logger.javaClass] = logger
    }

    override fun detach(logger: KLog.KLogger) {
        loggers.remove(logger.javaClass)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onLog(string: Spannable) {
        //TODO: Add Handlers
    }

}