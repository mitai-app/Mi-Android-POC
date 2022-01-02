package nyc.vonley.mi.ui.main.console

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType

interface ConsoleContract {

    interface View : BaseContract.View {
        fun onConsolesFound(consoles: List<Console>)
        fun onClientsFound(clients: List<Client>)
    }

    interface Presenter : BaseContract.Presenter {
        fun getConsoles(): List<Console>
    }

}