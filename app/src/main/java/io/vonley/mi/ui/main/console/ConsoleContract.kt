package io.vonley.mi.ui.main.console

import io.vonley.mi.base.BaseContract
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.models.Client

interface ConsoleContract {

    interface View : BaseContract.View, OnConsoleListener {
        fun addConsole()
        fun onConsoleAdded()
    }

    interface Presenter : BaseContract.Presenter, OnConsoleListener {
        val getTargetSummary: String
        fun getConsoles(): List<Client>
        fun addConsole(input: String)
        fun pin(client: Client)
        fun unpin(client: Client)
    }

}