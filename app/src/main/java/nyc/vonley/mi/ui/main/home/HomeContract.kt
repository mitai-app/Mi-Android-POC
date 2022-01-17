package nyc.vonley.mi.ui.main.home

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.di.network.MiJBServer

interface HomeContract {

    interface View : BaseContract.View, MiJBServer.MiJbServerListener {
        fun init(ip: String)
    }

    interface Presenter : BaseContract.Presenter {

    }

}