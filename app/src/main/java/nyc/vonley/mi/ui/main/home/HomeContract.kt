package nyc.vonley.mi.ui.main.home

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.di.network.MiJBServer
import nyc.vonley.mi.utils.SharedPreferenceManager

interface HomeContract {

    interface View : BaseContract.View, MiJBServer.MiJbServerListener {
        fun init(ip: String)
        fun openInfoDialog()
    }

    interface Presenter : BaseContract.Presenter {
        val manager: SharedPreferenceManager
    }

}