package io.vonley.mi.intents

import android.os.Binder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.SyncService
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PSXServiceBinder @Inject constructor() : Binder(), CoroutineScope {

    protected val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    @Inject
    lateinit var jb: MiServer

    @Inject
    lateinit var sync: SyncService

    companion object {
        val TAG = PSXServiceBinder::class.java.name
    }
}