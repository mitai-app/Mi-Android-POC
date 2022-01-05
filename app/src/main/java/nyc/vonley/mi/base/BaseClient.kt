package nyc.vonley.mi.base

import okhttp3.*

abstract class BaseClient(
    protected val http: OkHttpClient
) {

    fun post(url: String, body: RequestBody, headers: Headers, response: Callback) {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        val execute = http.newCall(req);
        execute.enqueue(response)
    }


}