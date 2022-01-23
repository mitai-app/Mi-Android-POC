package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Response
import java.io.DataInputStream
import java.io.InputStream
import java.util.ArrayList

interface PayloadContract {

    interface View : BaseContract.View {
        fun onPayloadSent(response: Response)
        fun open()
        fun onSending(payload: PayloadAdapter.Payload)
        fun onComplete(message: String)
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