package io.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import io.vonley.mi.R
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.extensions.toJson
import io.vonley.mi.intents.PSXService
import io.vonley.mi.models.OAuthToken
import io.vonley.mi.models.enums.Feature

/**
 *
 */
interface SharedPreferenceManager {

    val context: Context

    fun setId(user_id: Int)
    fun getId(): Int
    fun setToken(token: OAuthToken?)
    fun getToken(): OAuthToken?
    fun isLoggedIn(): Boolean
    fun logout(): Unit

    val sharedPreferences: SharedPreferences

    fun clear() = sharedPreferences.edit().clear().apply()


    var cached: Boolean
        get() = sharedPreferences.getBoolean(this[CACHED], false)
        set(value) {
            this[CACHED] = value
        }

    var voice: String?
        get() {
            return sharedPreferences.getString(this[VOICE], null)
        }
        set(voice) {
            val edit = sharedPreferences.edit()
            edit.putString(this[VOICE], voice)
            edit.apply()
        }

    val pitch: Float
        get() {
            return sharedPreferences.getInt(this[PITCH], 75) / 100f
        }


    val jbService: Boolean
        get() = sharedPreferences.getBoolean(this[MIJBSERVICE], true)

    var featurePort: Feature
        get() {
            val string = sharedPreferences.getString(
                this[MIJBFEATUREPORT],
                this[Feature.GOLDENHEN.id]
            )
            return enumValues<Feature>().find { p -> this[p.id] == string } ?: Feature.GOLDENHEN
        }
        set(port) {
            this[MIJBFEATUREPORT] = this[port.id]
        }

    var scanInterval: Int
        get() = sharedPreferences.getString(this[MISCANINTERVAL], "15")?.toInt() ?: 15
        set(interval) {
            this[MISCANINTERVAL] = interval
        }

    var update: PSXService.Meta?
        get() = sharedPreferences.getString(this[UPDATE], null)?.fromJson()
        set(value) {
            this[UPDATE] = value.toJson()
        }

    var jbPort: Int
        get() {
            return sharedPreferences.getString(
                this[MIJBSERVERPORT],
                "8080"
            )?.toInt() ?: 8080
        }
        set(port) {
            this[MIJBFEATUREPORT] = port
        }

    var ftpPath: String?
        get() {
            return sharedPreferences.getString(this[FTPPATH], "/")
        }
        set(path) {
            val edit = sharedPreferences.edit()
            edit.putString(this[FTPPATH], path)
            edit.apply()
        }

    var ftpUser: String?
        get() {
            return sharedPreferences.getString(this[FTPUSER], "")
        }
        set(user) {
            val edit = sharedPreferences.edit()
            edit.putString(this[FTPUSER], user)
            edit.apply()
        }

    var ftpPass: String?
        get() {
            return sharedPreferences.getString(this[FTPPASS], "")
        }
        set(pass) {
            val edit = sharedPreferences.edit()
            edit.putString(this[FTPPASS], pass)
            edit.apply()
        }

    var targetName: String?
        get() {
            return sharedPreferences.getString(this[TARGETNAME], null)
        }
        set(path) {
            val edit = sharedPreferences.edit()
            edit.putString(this[TARGETNAME], path)
            edit.apply()
        }

    var targetVersion: String?
        get() {
            return sharedPreferences.getString(this[TARGETVER], null)
        }
        set(path) {
            val edit = sharedPreferences.edit()
            edit.putString(this[TARGETVER], path)
            edit.apply()
        }

    val speed: Float
        get() {
            return sharedPreferences.getInt(this[SPEED], 100) / 100f
        }

    companion object {
        const val CACHED: Int = R.string.preference_jb_cache
        const val UUID: Int = R.string.preference_uuid
        const val VOICE: Int = R.string.preference_voices
        const val SPEED: Int = R.string.preference_speed
        const val PITCH: Int = R.string.preference_pitch
        const val FTPPATH: Int = R.string.preference_ftp_loc
        const val FTPUSER: Int = R.string.preference_ftp_user
        const val FTPPASS: Int = R.string.preference_ftp_pass
        const val TARGETNAME: Int = R.string.preference_target_name
        const val TARGETVER: Int = R.string.preference_target_version
        const val MIJBSERVERPORT: Int = R.string.preference_jb_port
        const val MISCANINTERVAL: Int = R.string.preference_jb_scan
        const val MIJBFEATUREPORT: Int = R.string.preference_jb_feature
        const val MIJBSERVICE: Int = R.string.preference_jb_service
        const val UPDATE: Int = R.string.preference_app_update
    }
}


operator fun SharedPreferenceManager.get(@StringRes string: Int): String {
    return context.getString(string)
}

inline operator fun <reified T> SharedPreferenceManager.get(string: String): T? {
    return when (T::class.java) {
        MutableSet<String>::javaClass -> sharedPreferences.getStringSet(string, setOf())
        String::javaClass -> sharedPreferences.getString(string, "")
        Boolean::javaClass -> sharedPreferences.getBoolean(string, false)
        Float::javaClass -> sharedPreferences.getFloat(string, 0f)
        Int::javaClass -> sharedPreferences.getInt(string, 0)
        Long::javaClass -> sharedPreferences.getLong(string, 0)
        else -> throw Throwable("Unsupported")
    } as? T
}

operator fun SharedPreferenceManager.set(@StringRes string: Int, value: String) {
    val edit = sharedPreferences.edit()
    edit.putString(this[string], value)
    edit.apply()
}

operator fun SharedPreferenceManager.set(@StringRes string: Int, value: Int) {
    val edit = sharedPreferences.edit()
    edit.putInt(this[string], value)
    edit.apply()
}

operator fun SharedPreferenceManager.set(@StringRes string: Int, value: Boolean) {
    val edit = sharedPreferences.edit()
    edit.putBoolean(this[string], value)
    edit.apply()
}

operator fun SharedPreferenceManager.set(@StringRes string: Int, value: Float) {
    val edit = sharedPreferences.edit()
    edit.putFloat(this[string], value)
    edit.apply()
}

operator fun SharedPreferenceManager.set(@StringRes string: Int, value: Long) {
    val edit = sharedPreferences.edit()
    edit.putLong(this[string], value)
    edit.apply()
}