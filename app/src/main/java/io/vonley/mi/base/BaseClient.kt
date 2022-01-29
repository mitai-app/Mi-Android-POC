package io.vonley.mi.base

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
        val execute = http.newCall(req)
        execute.enqueue(response)
    }

    fun post(url: String, body: RequestBody, headers: Headers): Response {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        val execute = http.newCall(req)
        return execute.execute()
    }

    fun get(url: String, response: Callback) {
        val req = Request.Builder()
            .url(url)
            .get()
            .build()
        val execute = http.newCall(req)
        execute.enqueue(response)
    }

}