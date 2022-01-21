package nyc.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import nyc.vonley.mi.R
import nyc.vonley.mi.models.OAuthToken
import nyc.vonley.mi.models.enums.Feature

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
        
    var featurePort: Feature
        get() {
            val string = sharedPreferences.getString(
                this[MIJBFEATUREPORT],
                this[Feature.GOLDENHEN.id]
            )
            return enumValues<Feature>().find { p -> this[p.id] == string }?:Feature.GOLDENHEN
        }
        set(port) {
            val edit = sharedPreferences.edit()
            edit.putString(this[MIJBFEATUREPORT], this[port.id])
            edit.apply()
        }

    var jbPort: Int
        get() {
            return sharedPreferences.getString(
                this[MIJBSERVERPORT],
                "8080"
            )?.toInt()?:8080
        }
        set(port) {
            val edit = sharedPreferences.edit()
            edit.putInt(this[MIJBSERVERPORT], port)
            edit.apply()
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
        const val MIJBFEATUREPORT: Int = R.string.preference_jb_feature
    }
}


operator fun SharedPreferenceManager.get(@StringRes string: Int): String {
    return context.getString(string)
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