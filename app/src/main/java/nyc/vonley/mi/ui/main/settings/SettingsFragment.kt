package nyc.vonley.mi.ui.main.settings

import android.os.Bundle
import android.view.View
import nyc.vonley.mi.R


import android.content.Context
import androidx.preference.*
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.databinding.FragmentSettingsBinding
import nyc.vonley.mi.di.modules.LocalStorageModule
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.ui.main.MainContract
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment @Inject constructor() : PreferenceFragmentCompat(), SettingsContract.View {


    val TAG = SettingsFragment::class.java.name

    @Inject
    lateinit var presenter: SettingsContract.Presenter

    private lateinit var shutdown: Preference
    private lateinit var rest: Preference
    private lateinit var reboot: Preference
    private lateinit var wifi: Preference
    private lateinit var clear: Preference
    private lateinit var version: Preference
    private lateinit var dev: Preference

    private lateinit var mainView: MainContract.View

    private var change: Preference.OnPreferenceClickListener =
        Preference.OnPreferenceClickListener { preference ->
            when (preference.key) {
                wifi.key -> {

                }
                rest.key -> {

                }
                reboot.key -> {
                }
                shutdown.key -> {
                }
                clear.key -> {
                }
                version.key -> {
                }
                dev.key -> {
                }
            }
            true

        }

    @Inject
    lateinit var sync: ClientSync


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = LocalStorageModule.PREFERENCE_FILE
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.preferences_mi, rootKey)
        wifi = preferenceScreen[getString(R.string.preference_wifi)]!!
        rest = preferenceScreen[getString(R.string.preference_rest)]!!
        reboot = preferenceScreen[getString(R.string.preference_reboot)]!!
        shutdown = preferenceScreen[getString(R.string.preference_shutdown)]!!
        clear = preferenceScreen[getString(R.string.preference_clear)]!!
        version = preferenceScreen[getString(R.string.preference_version)]!!
        dev = preferenceScreen[getString(R.string.preference_dev)]!!
        clear = preferenceScreen[getString(R.string.preference_clear)]!!

        wifi.onPreferenceClickListener = change
        rest.onPreferenceClickListener = change
        reboot.onPreferenceClickListener = change
        shutdown.onPreferenceClickListener = change
        clear.onPreferenceClickListener = change
        version.onPreferenceClickListener = change
        clear.onPreferenceClickListener = change
        version.onPreferenceClickListener = change
        wifi.title = "Wifi Info"
        wifi.summary = if (sync.isConnected)
            sync.connectionInfo.ssid
        else
            "Not connected"

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as MainContract.View)
    }

    override fun onError(e: Throwable) {

    }

    override fun onConsoleFound(console: Console) {

    }


}