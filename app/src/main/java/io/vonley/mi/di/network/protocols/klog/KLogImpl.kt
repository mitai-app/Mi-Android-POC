package io.vonley.mi.di.network.protocols.klog

import android.text.Spannable
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.extensions.d
import io.vonley.mi.extensions.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedReader
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class KLogImpl @Inject constructor(override val service: PSXService) : KLog, KLog.KLogger {

    private val job = Job()

    override val loggers: HashMap<Class<*>, KLog.KLogger> = hashMapOf(Pair(this.javaClass, this))

    private var _br: BufferedReader? = null

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
        string.d(TAG)
    }

    override suspend fun recv(): String? {
        return try {
            if (_br == null) {
                _br = socket.getInputStream().bufferedReader()
            }
            val readLine = _br?.readLine()
            readLine
        } catch (ex: Exception) {
            ex.printStackTrace()
            _br = null
            null
        }
    }

    override val TAG: String
        get() = KLogImpl::class.simpleName ?: KLogImpl::class.java.simpleName

}