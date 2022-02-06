package io.vonley.mi.ui.main.console.sheets

import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.network.PSXService
import javax.inject.Inject

class ProtocolPresenter @Inject constructor(
    val view: ProtocolContract.View,
    val service: PSXService
) : BasePresenter(), ProtocolContract.Presenter {


    override fun init() {
        service.initialize()
    }


    override fun cleanup() {

    }

    override val TAG: String
        get() = ProtocolPresenter::class.java.name
}