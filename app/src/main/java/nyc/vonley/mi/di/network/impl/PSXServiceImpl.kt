package nyc.vonley.mi.di.network.impl

import kotlinx.coroutines.*
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import nyc.vonley.mi.di.network.PSXService
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {



    override fun uploadBin(payloads: ArrayList<PayloadAdapter.Payload>, callback: PSXService.PSXListener) {
        launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, manager.featurePort.ports.first()))
                socket.getOutputStream().use { out ->
                    payloads.onEach { payload ->
                        withContext(Dispatchers.Main) {
                            callback.onWriting(payload)
                        }
                        out.write(payload.data)
                        out.flush()
                        delay(2000)
                        out.write(0)
                        out.flush()
                    }
                }
                socket.close()
                withContext(Dispatchers.Main) {
                    callback.onFinished()
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    callback.onSocketFailed()
                }
            }
        }
    }

    override val job: Job = Job()


}