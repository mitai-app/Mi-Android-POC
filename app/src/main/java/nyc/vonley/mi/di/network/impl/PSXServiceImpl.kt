package nyc.vonley.mi.di.network.impl

import kotlinx.coroutines.*
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import nyc.vonley.mi.di.network.PSXService
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.OkHttpClient
import okhttp3.internal.closeQuietly
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {


    override fun uploadBin(
        payloads: ArrayList<PayloadAdapter.Payload>,
        callback: PSXService.PSXListener
    ) {
        launch {
            payloads.onEach { payload ->
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(ip, manager.featurePort.ports.first()))
                    withContext(Dispatchers.Main) {
                        callback.onWriting(payload)
                    }
                    socket.getOutputStream().use { out ->
                        out.write(payload.data)
                        out.flush()
                    }
                    socket.closeQuietly()
                    withContext(Dispatchers.Main) {
                        payload.status = 1
                        callback.onSent(payload)
                    }
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        payload.status = -1
                        callback.onPayloadFailed(payload)
                    }
                }
                delay(3000)
            }
            withContext(Dispatchers.Main){
                callback.onFinished()
            }
        }
    }

    override val job: Job = Job()

}