package nyc.vonley.mi.di.network.impl

import nyc.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.di.network.PSXService
import okhttp3.OkHttpClient
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient
) : PSXService {

    
}