package nyc.vonley.mi.ui.main.console

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.di.network.listeners.OnConsoleListener
import nyc.vonley.mi.models.Client

interface ConsoleContract {

    interface View : BaseContract.View, OnConsoleListener {
        fun addConsole()
        fun onConsoleAdded()
    }

    interface Presenter : BaseContract.Presenter, OnConsoleListener {
        val getTargetSummary: String
        fun getConsoles(): List<Client>
        fun addConsole(input: String)
    }

}