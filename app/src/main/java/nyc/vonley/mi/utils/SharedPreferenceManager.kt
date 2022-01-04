package nyc.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import nyc.vonley.mi.R
import nyc.vonley.mi.models.OAuthToken

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

    val speed: Float
        get() {
            return sharedPreferences.getInt(this[SPEED], 100) / 100f
        }

    companion object {


        const val UUID: Int = R.string.preference_uuid
        const val VOICE: Int = R.string.preference_voices
        const val SPEED: Int = R.string.preference_speed
        const val PITCH: Int = R.string.preference_pitch

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