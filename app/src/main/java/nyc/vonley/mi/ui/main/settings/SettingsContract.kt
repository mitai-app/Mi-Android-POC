package nyc.vonley.mi.ui.main.settings

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.di.network.listeners.OnConsoleListener

interface SettingsContract {

    interface View : BaseContract.View {
        fun onCleared()
        fun initData()
    }

    interface Presenter : BaseContract.Presenter {
        fun clear()

    }

}