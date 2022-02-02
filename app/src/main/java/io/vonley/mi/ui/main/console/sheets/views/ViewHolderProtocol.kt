package io.vonley.mi.ui.main.console.sheets.views

import io.vonley.mi.di.network.protocols.common.PSXProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

interface ViewHolderProtocol<S : PSXProtocol> : CoroutineScope {
    val protocol: S

    fun init()

    private val job get() = Job()

    override val coroutineContext get() = Dispatchers.IO + job
}