package nyc.vonley.mi.base

import kotlinx.coroutines.CoroutineScope
import okhttp3.*

interface BaseClient: CoroutineScope {

    val http: OkHttpClient

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