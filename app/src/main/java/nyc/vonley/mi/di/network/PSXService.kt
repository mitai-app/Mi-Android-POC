package nyc.vonley.mi.di.network

import android.util.Log
import nyc.vonley.mi.base.BaseClient
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

interface PSXService : BaseClient {

    val sync: SyncService

    val manager: SharedPreferenceManager

    val target get() = sync.target

    val ip get() = target?.ip

    /**
     * Uploads file to web
     */
    fun uploadBin(file: ByteArray, callback: Callback) {
        if (target == null) return
        val url = "$ip:${manager.featurePort.ports.first()}"
        val body: RequestBody = file.toRequestBody()
        Log.e("URL", url)
        post(url, body, Headers.headersOf(), callback)
        /*
        if (target!!.features.contains(Feature.GOLDENHEN)) {
            Feature.GOLDENHEN.ports.map { "$ip:$it" }.onEach { url ->
                val body: RequestBody = file.toRequestBody()
                Log.e("URL", url)
                post(url, body, Headers.headersOf(), callback)
            }
        } else if (target!!.features.contains(Feature.ORBISAPI)) {
            Feature.ORBISAPI.ports.map { "$ip:$it" }.onEach { url ->
                val body: RequestBody = file.toRequestBody()
                Log.e("URL", url)
                post(url, body, Headers.headersOf(), callback)
            }
        }*/
    }

    /**
     * Uploads file to web
     */
    fun uploadBin(file: File, callback: Callback) {
        if (target == null) return
        val url = "$ip:${manager.featurePort.ports.first()}"
        val body: RequestBody = file.asRequestBody()
        Log.e("URL", url)
        post(url, body, Headers.headersOf(), callback)
        /*
        if (target!!.features.contains(Feature.GOLDENHEN)) {
            Feature.GOLDENHEN.ports.map { "$ip:$it" }.onEach { url ->
                val body: RequestBody = file.asRequestBody()
                Log.e("URL", url)
                post(url, body, Headers.headersOf(), callback)
            }
        } else if (target!!.features.contains(Feature.ORBISAPI)) {
            Feature.ORBISAPI.ports.map { "$ip:$it" }.onEach { url ->
                val body: RequestBody = file.asRequestBody()
                Log.e("URL", url)
                post(url, body, Headers.headersOf(), callback)
            }
        }*/
    }
}
