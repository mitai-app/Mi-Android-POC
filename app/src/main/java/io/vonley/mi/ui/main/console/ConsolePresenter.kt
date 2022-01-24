package io.vonley.mi.ui.main.console

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.models.Client
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.ConsoleType
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.persistence.ConsoleDao
import io.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

class ConsolePresenter @Inject constructor(
    val view: ConsoleContract.View,
    val sync: SyncService,
    val dao: ConsoleDao,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : BasePresenter(), ConsoleContract.Presenter {
    override val getTargetSummary: String
        get() = sync.target?.let { "Current Target: ${it.name} - ${it.ip}" }
            ?: "Current Target: none"

    override fun getConsoles(): List<Console> {
        return emptyList()
    }

    override fun addConsole(input: String) {
        launch {
            if (dao.exists(input)) {
                dao.updateNickName(input, "Playstation 4")
            } else {
                dao.insert(
                    Console(
                        input,
                        "Playstation 4",
                        ConsoleType.PS4,
                        listOf(Feature.FTP),
                        false,
                        sync.wifiInfo.ssid
                    )
                )
            }
            withContext(Dispatchers.Main) {
                view.onConsoleAdded()
            }
        }
    }

    override fun pin(client: Client) {
        launch {
            dao.setPin(client.ip, true)
        }
    }

    override fun unpin(client: Client) {
        launch {
            dao.setPin(client.ip, false)
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