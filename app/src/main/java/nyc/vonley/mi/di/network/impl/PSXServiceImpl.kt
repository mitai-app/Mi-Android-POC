package nyc.vonley.mi.di.network.impl

import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import nyc.vonley.mi.di.network.PSXService
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.OkHttpClient
import javax.inject.Inject

class PSXServiceImpl @Inject constructor(
    override val sync: SyncService,
    @GuestInterceptorOkHttpClient override val http: OkHttpClient,
    @SharedPreferenceStorage override val manager: SharedPreferenceManager
) : PSXService {


}