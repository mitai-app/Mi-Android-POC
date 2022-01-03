package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val sync: ClientSync
) : BasePresenter(),
    PayloadContract.Presenter {


    override fun sendPayload(bytes: ByteArray) {

    }

    override fun init() {

    }

    override fun cleanup() {

    }
}