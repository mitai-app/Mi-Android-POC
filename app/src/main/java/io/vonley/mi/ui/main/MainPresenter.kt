package io.vonley.mi.ui.main

import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.models.enums.PlatformType
import javax.inject.Inject

class MainPresenter @Inject constructor(
    val view: MainContract.View,
    val sync: SyncService
) : BasePresenter(),
    MainContract.Presenter {

    override fun getConsoles(platform: PlatformType) {

    }

    override fun init() {
        view.setTitle("ミ - PS4 & PS3 Remote Tool")
        view.setSummary("Current Target: none")
    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = MainPresenter::class.java.name

}