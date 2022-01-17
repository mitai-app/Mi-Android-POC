package nyc.vonley.mi.ui.main.home

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.MiJBServer
import javax.inject.Inject

class HomePresenter @Inject constructor(val view: HomeContract.View, val jb: MiJBServer) :
    BasePresenter(), HomeContract.Presenter {


    override fun init() {
        jb.add(view)
        view.init(jb.service.sync.ipAddress)
    }

    override fun cleanup() {
        jb.remove(view)
    }

    override val TAG: String
        get() = HomePresenter::class.java.name


}