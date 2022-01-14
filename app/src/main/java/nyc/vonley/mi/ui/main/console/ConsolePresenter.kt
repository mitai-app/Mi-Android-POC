package nyc.vonley.mi.ui.main.console

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.SyncService

import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console

import javax.inject.Inject

class ConsolePresenter @Inject constructor(
    val view: ConsoleContract.View,
    val sync: SyncService
) : BasePresenter(), ConsoleContract.Presenter {

    override fun getConsoles(): List<Console> {
        return emptyList()
    }

    override fun init() {
        sync.addConsoleListener(this)
        sync.getClients()
    }

    override val TAG: String
        get() = ConsolePresenter::class.java.name

    override fun onClientsFound(clients: List<Client>) {
        view.onClientsFound(clients)
    }

    override fun onEmptyDataReceived() {
        view.onEmptyDataReceived()
    }

    override fun onAlreadyStored() {

    }

    override fun cleanup() {

    }
}