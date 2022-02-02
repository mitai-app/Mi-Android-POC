package io.vonley.mi.extensions

import android.util.Log
import io.vonley.mi.BuildConfig

fun <T> T.e(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, this.toString(), e)
    }
}

fun <T> T.i(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, this.toString(), e)
    }
}

fun <T> T.d(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, this.toString(), e)
    }
}

fun <T> T.v(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, this.toString(), e)
    }
}

fun <T> T.wtf(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.wtf(tag, this.toString(), e)
    }
}

fun <T> T.w(tag: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.w(tag, this.toString(), e)
    }
}
