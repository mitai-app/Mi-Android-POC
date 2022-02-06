package io.vonley.mi.ui.main.payload

import io.vonley.mi.base.BaseContract
import io.vonley.mi.di.network.callbacks.PayloadCallback
import io.vonley.mi.models.Payload
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Response
import java.util.ArrayList

interface PayloadContract {

    interface View : BaseContract.View, PayloadCallback {
        fun onPayloadSent(response: Response)
        fun open()
    }

    interface Presenter : BaseContract.Presenter {
        val manager: SharedPreferenceManager

        fun sendMultiplePayloads(payloads: ArrayList<Payload>)
    }

}