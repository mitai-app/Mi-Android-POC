package nyc.vonley.mi.ui.main

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType

interface MainContract {

    interface View : BaseContract.View {
        fun onConsolesFound(consoles: List<Console>)
        fun setTitle(title: String?)
        fun setSummary(summary: String?)
    }

    interface Presenter : BaseContract.Presenter {
        fun getConsoles(console: ConsoleType);
    }

}