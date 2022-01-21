package nyc.vonley.mi.ui.main.payload

import nyc.vonley.mi.base.BasePresenter
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.impl.PSXServiceImpl
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class PayloadPresenter @Inject constructor(
    val view: PayloadContract.View,
    val ps4: PSXServiceImpl,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : BasePresenter(),
    PayloadContract.Presenter {

    override fun sendPayload(bytes: ByteArray) {
        ps4.uploadBin(bytes, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                view.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                view.onPayloadSent(response)
            }
        })
    }

    override fun init() {

    }

    override fun cleanup() {

    }

    override val TAG: String
        get() = this::class.java.name

}