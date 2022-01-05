package nyc.vonley.mi.di.network.impl

import android.util.Log
import nyc.vonley.mi.base.BaseClient
import nyc.vonley.mi.di.modules.GuestInterceptorOkHttpClient
import nyc.vonley.mi.di.network.ClientSync
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PS4ClientService @Inject constructor(
    val sync: ClientSync,
    @GuestInterceptorOkHttpClient http: OkHttpClient
) : BaseClient(http) {

    val target get() = sync.target

    val ip get() = target?.ip


    val callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("RESPONSE", e.message ?: "Something happened", e)
        }

        override fun onResponse(call: Call, response: Response) {
            Log.e("RESPONSE", response.message)
        }

    }

    /**
     * Uploads file to web
     */
    fun uploadBin(file: ByteArray) {
        val url = "http://$ip:9090"
        Log.e("ERROR","TARGET IS $url")
        if (target == null) {
            Log.e("ERROR","TARGET IS NULL")
            return
        }
        val body: RequestBody = file.toRequestBody()
        Log.e("URL", url)
        post(url, body, Headers.headersOf(), callback)
    }

    /**
     * Uploads file to web
     */
    fun uploadBin(file: File) {
        if (target == null) return
        val body: RequestBody = file.asRequestBody()
        val url = "$ip:9090"
        post(url, body, Headers.headersOf(), callback)
    }


}