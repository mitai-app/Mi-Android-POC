package nyc.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import nyc.vonley.mi.models.OAuthToken

class SharedPreferenceManagerImpl(
    override val context: Context,
    override val sharedPreferences: SharedPreferences
) : SharedPreferenceManager {

    override fun setId(user_id: Int) {
        val edit = sharedPreferences.edit()
        edit.putInt(USER_ID, user_id)
        edit.apply()
        Log.e(tag(), "LOGGED USER ID: ($user_id)")
    }

    override fun getId(): Int {
        return sharedPreferences.getInt(USER_ID, 0)
    }

    override fun isLoggedIn(): Boolean {
        return false
    }


    override fun setToken(token: OAuthToken?) {
        if (token == null) return
        try {
            val edit = sharedPreferences.edit()
            val tokenJson = Gson().toJson(token)
            edit.putString(OAUTH2_TOKEN, tokenJson)
            edit.apply()
            Log.e(
                tag(),
                "SAVED USER USER TOKEN: (${token.token_type}) - (${
                    token.access_token.substring(
                        0,
                        16
                    )
                }"
            )
        } catch (e: Exception) {
            Log.e(tag(), "Error2", e)
        }
    }

    override fun getToken(): OAuthToken? {
        val tokenJson = sharedPreferences.getString(OAUTH2_TOKEN, null)
        return if (!tokenJson.isNullOrEmpty()) Gson().fromJson(
            tokenJson,
            OAuthToken::class.java
        ) else null
    }

    override fun logout() {
        val edit = sharedPreferences.edit()
        edit.clear()
        edit.apply()
    }


    companion object {
        fun tag(): String {
            return this::class.toString()
        }

        private const val UUID: String = "nyc.vonley.mi.UUID"
        private const val USER: String = "nyc.vonley.mi.USER"
        private const val PROFILE: String = "nyc.vonley.mi.PROILE"
        private const val PROFILE_ID: String = "nyc.vonley.mi.PROILE.ID"
        private const val USER_ID: String = "nyc.vonley.leathis.USER.ID"
        private const val OAUTH2_TOKEN: String = "nyc.vonley.mi.OAUTH2.TOKEN"
        private const val COOKIE_KEY_ENCRYPTED: String = "nyc.vonley.mi.COOKIE.TOKEN"

    }
}