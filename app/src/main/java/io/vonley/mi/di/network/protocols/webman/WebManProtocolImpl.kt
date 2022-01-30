package io.vonley.mi.di.network.protocols.webman

import androidx.lifecycle.LiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIProtocol
import io.vonley.mi.di.network.protocols.common.models.Process
import kotlin.coroutines.CoroutineContext

class WebManProtocolImpl(override val service: PSXService): WebManProtocol{

    override val processes: List<Process>
        get() = TODO("Not yet implemented")

    override val liveProcesses: LiveData<List<Process>>
        get() = TODO("Not yet implemented")

    override var attached: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var process: Process?
        get() = TODO("Not yet implemented")
        set(value) {}


    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
}