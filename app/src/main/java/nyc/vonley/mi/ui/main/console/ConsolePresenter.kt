package nyc.vonley.mi.ui.main.console

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import javax.inject.Inject

class ConsolePresenter @Inject constructor(
    val view: ConsoleContract.View,
    val sync: ClientSync
) : BasePresenter(),
    ConsoleContract.Presenter {

    override fun getConsoles(): List<Console> {
        return sync.activeConsoles
    }


    override fun init() {
        val callableClients: (clients: List<Client>) -> Unit = { clients ->
            view.onClientsFound(clients)
        }
        val callableConsoles: (consoles: List<Console>) -> Unit = { consoles ->
            view.onConsolesFound(consoles)
        }
        sync.addListener(this)
        sync.getClients(callableClients, callableConsoles)
    }

    override fun cleanup() {

    }

    override fun onConsoleFound(console: Console) {
        view.onConsoleFound(console)
    }
}