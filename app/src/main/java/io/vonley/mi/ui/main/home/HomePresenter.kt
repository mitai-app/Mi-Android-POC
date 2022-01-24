package io.vonley.mi.ui.main.home

import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

class HomePresenter @Inject constructor(
    val view: HomeContract.View,
    val jb: MiServer,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : BasePresenter(), HomeContract.Presenter {


    override fun init() {
        jb.add(view)
        view.init("${jb.sync.ipAddress}:${jb.activePort}")
    }

    override fun cleanup() {
        jb.remove(view)
    }

    override val TAG: String
        get() = HomePresenter::class.java.name


}