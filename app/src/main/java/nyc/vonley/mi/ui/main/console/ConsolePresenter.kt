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
        sync.getClients { clients, consoles ->
            view.onConsolesFound(consoles)
        }
    }

    override fun cleanup() {

    }
}