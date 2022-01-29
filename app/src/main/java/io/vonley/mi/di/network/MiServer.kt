package io.vonley.mi.di.network

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import io.vonley.mi.BuildConfig
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.ui.main.payload.adapters.PayloadAdapter

interface MiServer : CoroutineScope {
    val activePort: Int
    val service: PSXService
    var server: NanoHTTPD?
    fun stopService()
    fun startService()
    fun restartService() {
        launch {
            try {
                Log.e("MiServer","Stopping....")
                while (server?.isAlive == true) {
                    stopService()
                    delay(2000)
                }
                Log.e("MiServer","Restarting....")
                withContext(Dispatchers.Main){
                    startService()
                }
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e("MiServer", "could not stop hmmm ${e.message}")
                }
            }
        }
    }

    fun add(jb: MiServerImpl.MiJbServerListener)
    fun remove(jb: MiServerImpl.MiJbServerListener)
    fun hostPackage(vararg payload: PayloadAdapter.Payload): Array<String>
}