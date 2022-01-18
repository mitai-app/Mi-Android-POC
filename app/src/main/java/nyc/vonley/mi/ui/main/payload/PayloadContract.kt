package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BaseContract
import okhttp3.Response
import java.io.DataInputStream
import java.io.InputStream

interface PayloadContract {

    interface View : BaseContract.View {
        fun onPayloadSent(response: Response)

    }

    interface Presenter : BaseContract.Presenter {
        fun sendPayload(bytes: ByteArray)
        fun sendPayload(stream: InputStream) {
            val bytes = DataInputStream(stream).use {
                it.readBytes()
            }
            return sendPayload(bytes)
        }
    }

}