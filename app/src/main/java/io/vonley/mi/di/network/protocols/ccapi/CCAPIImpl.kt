package io.vonley.mi.di.network.protocols.ccapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.models.ConsoleInfo
import io.vonley.mi.di.network.protocols.common.models.ConsoleType
import io.vonley.mi.di.network.protocols.common.models.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class CCAPIImpl(override val service: PSXService) : CCAPI {

    private val _processes =  arrayListOf<Process>()
    override val processes: List<Process>
        get() = _processes
    private val _liveProcesses =  MutableLiveData<List<Process>>()
    override val liveProcesses: LiveData<List<Process>>
        get() = _liveProcesses
    override var attached: Boolean = false
    override var process: Process? = null

    override suspend fun getPids(): List<Process> {
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



    //region Private Methods
    @Throws(IOException::class)
    private suspend fun getFirmwareInfo(): List<String> {
        return getListRequest(compileUrl(CCAPI.Builder.firmWareInfo))
    }

    @Throws(IOException::class)
    private suspend fun getProcessList(): List<String> {
        return getListRequest(compileUrl(CCAPI.Builder.processList))
    }

    @Throws(IOException::class)
    private suspend fun getProcessName(pid: String): List<String> {
        return getListRequest(compileUrl(CCAPI.Builder.getProcessName(pid)))
    }

    override suspend fun getConsoleInfo(): ConsoleInfo?  {
        return try {
            val temperature = getTemperature()
            val firmware = getFirmwareInfo()
            var firm: String = ""
            var occur = 0
            for (c in firmware[1].substring(0, 4).toCharArray()) {
                if (c == '0' && occur < 1) {
                    firm += "."
                    occur++
                } else firm += c
            }
            ConsoleInfo(firm, ConsoleType.values()[firmware[3].toInt()], temperature)
        }catch (e: Throwable) {
            null
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = Job()

}