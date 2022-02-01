package io.vonley.mi.di.network.protocols.webman

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.models.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class WebManImpl(override val service: PSXService) : WebMan {

    private val _liveProcesses = MutableLiveData<List<Process>>()

    private val _processes = arrayListOf<Process>()

    override var attached: Boolean = false

    override var process: Process? = null

    override val processes: List<Process>
        get() = _processes

    override val liveProcesses: LiveData<List<Process>>
        get() = _liveProcesses

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = Job()
}