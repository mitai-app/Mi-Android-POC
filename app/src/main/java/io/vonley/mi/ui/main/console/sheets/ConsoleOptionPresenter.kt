package io.vonley.mi.ui.main.console.sheets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.base.BaseContract
import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.get
import io.vonley.mi.di.network.impl.set
import io.vonley.mi.models.enums.ConsoleType
import io.vonley.mi.models.enums.Feature
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll
import javax.inject.Inject

class ConsoleOptionPresenter @Inject constructor(
    val view: ConsoleOptionContract.View,
    val service: PSXService
) : BasePresenter(), BaseContract.Presenter {


    override fun init() {
        service.initialize()
    }


    override fun cleanup() {

    }

    override val TAG: String
        get() = ConsoleOptionPresenter::class.java.name
}