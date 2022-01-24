package io.vonley.mi.ui.main.home

import io.vonley.mi.base.BaseContract
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.utils.SharedPreferenceManager

interface HomeContract {

    interface View : BaseContract.View, MiServerImpl.MiJbServerListener {
        fun init(ip: String)
        fun openInfoDialog()
    }

    interface Presenter : BaseContract.Presenter {
        val manager: SharedPreferenceManager
    }

}