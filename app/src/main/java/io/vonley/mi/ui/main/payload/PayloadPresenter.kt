package io.vonley.mi.ui.main.payload

import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.ArrayList
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val ps4: PSXServiceImpl,
    val server: MiServer,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : BasePresenter(),
    PayloadContract.Presenter {

    override fun sendMultiplePayloads(payloads: ArrayList<PayloadAdapter.Payload>) {
        ps4.uploadBin(server, payloads, view)
    }

    override fun init() {

    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = this::class.java.name

}