package io.vonley.mi.ui.main.console.sheets

import io.vonley.mi.base.BaseContract
import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.network.PSXService
import javax.inject.Inject

class ConsoleOptionPresenter @Inject constructor(
    val view: ConsoleOptionContract.View,
    val service: PSXService
) : BasePresenter(), ConsoleOptionContract.Presenter {


    override fun init() {
        service.initialize()
    }


    override fun cleanup() {

    }

    override val TAG: String
        get() = ConsoleOptionPresenter::class.java.name
}