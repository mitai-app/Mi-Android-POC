package io.vonley.mi.ui.main.payload

import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.di.network.protocols.goldenhen.Goldhen
import io.vonley.mi.models.Payload
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val ps4: PSXServiceImpl,
    val server: MiServer,
    val goldHen: Goldhen,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : BasePresenter(),
    PayloadContract.Presenter {

    override fun sendMultiplePayloads(payloads: ArrayList<Payload>) {
        launch {
            goldHen.sendPayloads(view, *payloads.toTypedArray())
            withContext(Dispatchers.Main) {
                view.onFinished()
            }
        }
    }

    override fun init() {

    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = this::class.java.name

}