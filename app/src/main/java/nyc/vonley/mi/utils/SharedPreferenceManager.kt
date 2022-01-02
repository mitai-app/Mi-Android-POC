package nyc.vonley.mi.utils

import nyc.vonley.mi.models.OAuthToken

/**
 *
 */
interface SharedPreferenceManager {

    fun setId(user_id: Int)
    fun getId(): Int
    fun setToken(token: OAuthToken?)
    fun getToken(): OAuthToken?
    fun isLoggedIn(): Boolean
    fun logout(): Unit
}