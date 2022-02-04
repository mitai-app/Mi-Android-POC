package io.vonley.mi.di.network.protocols.webman

import io.vonley.mi.di.network.PSXService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class WebManImpl(override val service: PSXService) : Webman {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + job
}