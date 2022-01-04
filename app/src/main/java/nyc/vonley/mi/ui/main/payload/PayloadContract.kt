package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType

interface PayloadContract {

    interface View : BaseContract.View {

    }

    interface Presenter : BaseContract.Presenter {
        fun sendPayload(bytes: ByteArray)
    }

}