package nyc.vonley.mi.ui.main

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.models.enums.ConsoleType
import javax.inject.Inject

class MainPresenter @Inject constructor(
    val view: MainContract.View,
    val sync: SyncService
) : BasePresenter(),
    MainContract.Presenter {

    override fun getConsoles(console: ConsoleType) {

    }

    override fun init() {
        view.setTitle(sync.wifiInfo.ssid)
        view.setSummary(sync.ipAddress)
    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = MainPresenter::class.java.name

}