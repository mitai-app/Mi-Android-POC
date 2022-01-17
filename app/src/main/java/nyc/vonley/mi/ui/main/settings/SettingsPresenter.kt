package nyc.vonley.mi.ui.main.settings

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.persistence.AppDatabase
import javax.inject.Inject

class SettingsPresenter @Inject constructor(val db: AppDatabase) : BasePresenter(), SettingsContract.Presenter {


    override fun init() {

    }

    override fun cleanup() {
        
    }

    override val TAG: String
        get() = TODO("Not yet implemented")

}