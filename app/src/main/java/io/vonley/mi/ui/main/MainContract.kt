package io.vonley.mi.ui.main

import io.vonley.mi.base.BaseContract
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.ConsoleType

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