package nyc.vonley.mi.ui.main

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.enums.ConsoleType
import javax.inject.Inject

class MainPresenter @Inject constructor(val view: MainContract.View) : BasePresenter(),
    MainContract.Presenter {

    override fun getConsoles(console: ConsoleType) {

    }

    override fun cleanup() {

    }
}