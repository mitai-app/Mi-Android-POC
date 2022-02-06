package io.vonley.mi.ui.main

import io.vonley.mi.base.BaseContract
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.PlatformType

interface MainContract {

    interface View : BaseContract.View, KLog.KLogger {
        fun onConsolesFound(consoles: List<Console>)
        fun setTitle(title: String?)
        fun setSummary(summary: String?)
    }

    interface Presenter : BaseContract.Presenter {
        fun getConsoles(platform: PlatformType);
    }

}