package io.vonley.mi.base

import io.vonley.mi.BuildConfig
import io.vonley.mi.extensions.e
import kotlinx.coroutines.CoroutineScope
import okhttp3.*

interface BaseClient : CoroutineScope {

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

    fun post(url: String, body: RequestBody, headers: Headers): Response? {
        val req = Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build()
        val execute = http.newCall(req)
        return try {
            execute.execute()
        } catch (e: Throwable) {
            "Something went wrong: ${e.message}".e(TAG, e)
            null
        }
    }

    fun getRequest(url: String, response: Callback) {
        val req = Request.Builder()
            .url(url)
            .get()
            .build()
        val execute = http.newCall(req)
        execute.enqueue(response)
    }

    fun getRequest(url: String): Response? {
        val req = Request.Builder()
            .url(url)
            .get()
            .build()
        val execute = http.newCall(req)
        return try {
            execute.execute()
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                "Something went wrong: ${e.message}".e(TAG, e)
            }
            null
        }
    }

    val TAG: String
        get() = BaseClient::class.java.name

}

