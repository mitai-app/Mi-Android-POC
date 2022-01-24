package io.vonley.mi.intents

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.BuildConfig
import io.vonley.mi.base.BaseClient
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.extensions.toJson
import io.vonley.mi.utils.Semver
import io.vonley.mi.utils.SharedPreferenceManager
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PSXService : Service(), BaseClient {


    val TAG = PSXService::class.java.name

    @Inject
    lateinit var binder: PSXServiceBinder


    override val http: OkHttpClient get() = binder.jb.service.http

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    var check = true
    var meta: Meta? = null
    val seconds = 15

    data class Meta(val version: String, val changes: String, val build: String) {
        override fun toString(): String {
            return GsonBuilder().create().toJson(this)
        }
    }

    private var update: Job? = null
    private fun checkforUpdates() {
        val block: suspend CoroutineScope.() -> Unit = {
            do {
                if (meta != null) {
                    manager.update = meta
                    continue
                }
                get(
                    "https://raw.githubusercontent.com/Mr-Smithy-x/Mi/main/meta.json",
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.body?.let { body ->
                                val json = JSONObject(body.string())
                                val version = json.getString("version")
                                if (version.ver > BuildConfig.VERSION_NAME.ver) {
                                    val changesList = json.getJSONArray("changes")
                                    val build = json.getString("build")
                                    val changes = StringBuilder()
                                    for (i in 0..changesList.length()) {
                                        changes.append(changesList[i].toString() + "\n")
                                    }
                                    this@PSXService.meta = Meta(version, changes.toString(), build)
                                }

                            } ?: run {

                            }
                        }

                    })
                delay((seconds * 1000L))
            } while (check)
        }
        when {
            update == null -> {
                update = launch(block = block)
            }
            update?.isCancelled == true -> {
                update = launch(block = block)
            }
            update?.isCompleted == true -> {
                update = launch(block = block)
            }
            update?.isActive == true -> {
                //Do nothing
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        if (manager.jbService) {
            binder.jb.startService()
        }
        binder.sync.getClients(true)
        checkforUpdates()
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        binder.sync.stop()
        binder.jb.stopService()
        update?.cancel()
        check = false
        super.onDestroy()
    }

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


}

val String.ver: Semver
    get() = Semver(this)