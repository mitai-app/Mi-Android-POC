package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import okhttp3.Response

interface PayloadContract {

    interface View : BaseContract.View {
        fun onPayloadSent(response: Response)

    }

    interface Presenter : BaseContract.Presenter {
        fun sendPayload(bytes: ByteArray)
    }

}