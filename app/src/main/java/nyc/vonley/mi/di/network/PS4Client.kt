package nyc.vonley.mi.di.network

import android.content.Context
import nyc.vonley.mi.base.BaseClient
import nyc.vonley.mi.di.annotations.GuestRetrofitClient
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PS4Client constructor(
    protected var ip: String,
    protected var port: Int,
    @GuestRetrofitClient client: OkHttpClient
) : BaseClient(client) {


    /**
     * Uploads file to web
     */
    fun uploadBin(file: File) {
        val body: RequestBody = file.asRequestBody()
        post("$ip:$port", body, Headers.headersOf())
    }

    companion object {
        fun findConsole(context: Context) {

        }
    }

}