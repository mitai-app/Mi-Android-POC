package io.vonley.mi.ui.main.payload

import io.vonley.mi.base.BaseContract
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Response
import java.io.DataInputStream
import java.io.InputStream
import java.util.ArrayList

interface PayloadContract {

    interface View : BaseContract.View, PSXService.PSXListener {
        fun onPayloadSent(response: Response)
        fun open()
    }

    interface Presenter : BaseContract.Presenter {
        val manager: SharedPreferenceManager

        fun sendPayload(bytes: ByteArray)
        fun sendPayload(stream: InputStream) {
            val bytes = DataInputStream(stream).use {
                it.readBytes()
            }
            return sendPayload(bytes)
        }
        fun sendMultiplePayloads(payloads: ArrayList<PayloadAdapter.Payload>)
    }

}