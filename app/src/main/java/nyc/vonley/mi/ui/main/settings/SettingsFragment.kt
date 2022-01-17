package nyc.vonley.mi.ui.main.settings


import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.get
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.R
import nyc.vonley.mi.di.modules.LocalStorageModule
import nyc.vonley.mi.di.network.SyncService
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
                wifi.key -> {}
                rest.key,
                reboot.key,
                shutdown.key -> {
                    Toast.makeText(
                        requireContext(),
                        "Haven't found a way yet unfortunately",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                clear.key -> {
                    presenter.cleanup()
                }
                version.key -> {}
                dev.key -> {}
            }
            true

        }

    @Inject
    lateinit var sync: SyncService


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
        val title: CharSequence = if (sync.isConnected)
            sync.wifiInfo.ssid?:"Unknown SSID"
        else
            "Not connected"

        val summary: CharSequence = if(sync.isConnected) sync.ipAddress else "Not connected"
        wifi.title = title
        wifi.summary = summary
        version.summary = BuildConfig.VERSION_NAME
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as MainContract.View)
    }

    override fun onError(e: Throwable) = Unit


}