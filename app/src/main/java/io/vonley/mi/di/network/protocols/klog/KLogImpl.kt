package io.vonley.mi.di.network.protocols.klog

import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class KLogImpl @Inject constructor(override val service: PSXService) : KLog {

    private val job = Job()

    override val logger: KLog.KLogger = object: KLog.KLogger {
        override fun onLog(string: String) {
            string.e(TAG)
        }
    }

    override var onGoing: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}