package nyc.vonley.mi.ui.main.console

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.persistence.ConsoleDao
import nyc.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

class ConsolePresenter @Inject constructor(
    val view: ConsoleContract.View,
    val sync: SyncService,
    val dao: ConsoleDao,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : BasePresenter(), ConsoleContract.Presenter {
    override val getTargetSummary: String
        get() = sync.target?.let { "Current Target: ${it.name} - ${it.ip}" } ?: "Current Target: none"

    override fun getConsoles(): List<Console> {
        return emptyList()
    }

    override fun addConsole(input: String) {
        launch {
            if (dao.exists(input)) {
                dao.updateNickName(input, "Playstation 4")
            } else {
                dao.add(
                    input,
                    "Playstation 4",
                    ConsoleType.PS4,
                    listOf(),
                    false,
                    sync.wifiInfo.ssid
                )
            }
            withContext(Dispatchers.Main) {
                view.onConsoleAdded()
            }
        }
    }

    override fun init() {
        sync.addConsoleListener(this)
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