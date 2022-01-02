package nyc.vonley.mi.base

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

abstract class BaseClient(
    protected val http: OkHttpClient
) {

    fun post(url: String, body: RequestBody, headers: Headers) {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        val execute = http.newCall(req).execute()
    }


}