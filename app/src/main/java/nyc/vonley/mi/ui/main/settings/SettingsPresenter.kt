package nyc.vonley.mi.ui.main.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.MiServer
import nyc.vonley.mi.persistence.AppDatabase
import nyc.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    val view: SettingsContract.View,
    val db: AppDatabase,
    @SharedPreferenceStorage val manager: SharedPreferenceManager,
    val service: MiServer
) : BasePresenter(), SettingsContract.Presenter {
    override fun clear() {
        launch {
            manager.clear()
            db.clearAllTables()
            withContext(Dispatchers.Main) {
                view.onCleared()
                view.initData()
            }
        }
    }

    override fun start() {
        service.startService()
    }

    override fun stop() {
        service.stopService()
    }

    override fun restart() {
        service.restartService()
    }

    override fun init() {

    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = TODO("Not yet implemented")

}