package nyc.vonley.mi.di.network


import android.accounts.Account
import android.accounts.AccountManager
import nyc.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class OAuth2Authenticator constructor(val manager: SharedPreferenceManager) : Authenticator {
    /**
     * For real account security look into this
     * @see AccountManager
     * @see Account
     */
    override fun authenticate(route: Route?, response: Response): Request {
        return response.request
    }




}