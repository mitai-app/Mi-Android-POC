package nyc.vonley.mi.ui.main

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.enums.ConsoleType
import javax.inject.Inject

class MainPresenter @Inject constructor(
    val view: MainContract.View,
    val sync: ClientSync
) : BasePresenter(),
    MainContract.Presenter {

    override fun getConsoles(console: ConsoleType) {

    }

    override fun init() {
        view.setTitle(sync.connectionInfo.ssid)
        view.setSummary(sync.ip)
    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = MainPresenter::class.java.name

}