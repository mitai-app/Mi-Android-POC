package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.Console
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val sync: ClientSync
) : BasePresenter(),
    PayloadContract.Presenter {

    override fun sendPayload(bytes: ByteArray) {

    }

    override fun init() {
        sync.addListener(this)
    }

    override fun cleanup() {

    }

    override fun onConsoleFound(console: Console) {
        view.onConsoleFound(console)
    }
}