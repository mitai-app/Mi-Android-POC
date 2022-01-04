package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.network.impl.ClientSyncService
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val sync: ClientSyncService
) : BasePresenter(),
    PayloadContract.Presenter {

    override fun sendPayload(bytes: ByteArray) {

    }

    override fun init() {
        //sync.initialize()
        //sync.addConsoleListener(this)
    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = this::class.java.name

    /*
    override fun onConsoleFound(console: Console) {
        view.onConsoleFound(console)
    }*/
}