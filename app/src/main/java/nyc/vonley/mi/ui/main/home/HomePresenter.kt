package nyc.vonley.mi.ui.main.home

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.MiServer
import nyc.vonley.mi.di.network.impl.MiServerImpl
import nyc.vonley.mi.utils.SharedPreferenceManager
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