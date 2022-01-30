package io.vonley.mi.di.network.protocols.ccapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIProtocol
import io.vonley.mi.di.network.protocols.ps3mapi.models.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CCAPIProtocolImpl(override val service: PSXService) : CCAPIProtocol {
    override val listener: PS3MAPIProtocol.JMAPIListener
        get() = TODO("Not yet implemented")

    private val _processes =  arrayListOf<Process>()
    override val processes: List<Process>
        get() = _processes
    private val _liveProcesses =  MutableLiveData<List<Process>>()
    override val liveProcesses: LiveData<List<Process>>
        get() = _liveProcesses
    override var attached: Boolean = false
    override var process: Process? = null

    override val pids: List<Process>
        get() {
            val processList: MutableList<Process> = ArrayList()
            for (process in getProcessList()) {
                if (process != "0") {
                    for (name in getProcessName(process)) {
                        if (name != "0") {
                            processList.add(Process(name, process))
                        }
                    }
                }
            }
            return processList
        }

    override fun notify(message: String) {

    }

    override fun boot(ps3boot: Boot) {

    }

    val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

}