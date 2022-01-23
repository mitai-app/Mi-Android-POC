package nyc.vonley.mi.ui.main.settings


import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.R
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.modules.LocalStorageModule
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment @Inject constructor() : PreferenceFragmentCompat(), SettingsContract.View,
    Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    val TAG = SettingsFragment::class.java.name

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager

    @Inject
    lateinit var presenter: SettingsContract.Presenter

    private lateinit var shutdown: Preference
    private lateinit var rest: Preference
    private lateinit var reboot: Preference
    private lateinit var wifi: Preference
    private lateinit var clear: Preference
    private lateinit var version: Preference
    private lateinit var targetVersion: Preference
    private lateinit var targetName: Preference
    private lateinit var ftpUser: EditTextPreference
    private lateinit var ftpPass: EditTextPreference
    private lateinit var features: ListPreference
    private lateinit var port: EditTextPreference
    private lateinit var dev: Preference
    private lateinit var scan: ListPreference
    private lateinit var service: SwitchPreferenceCompat

    private var mainView: MainContract.View? = null

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
        ftpUser = preferenceScreen[getString(R.string.preference_ftp_user)]!!
        ftpPass = preferenceScreen[getString(R.string.preference_ftp_pass)]!!
        targetName = preferenceScreen[getString(R.string.preference_target_name)]!!
        targetVersion = preferenceScreen[getString(R.string.preference_target_version)]!!
        features = preferenceScreen[getString(R.string.preference_jb_feature)]!!
        port = preferenceScreen[getString(R.string.preference_jb_port)]!!
        service = preferenceScreen[getString(R.string.preference_jb_service)]!!
        scan = preferenceScreen[getString(R.string.preference_jb_scan)]!!

        wifi.onPreferenceClickListener = this
        rest.onPreferenceClickListener = this
        reboot.onPreferenceClickListener = this
        shutdown.onPreferenceClickListener = this
        clear.onPreferenceClickListener = this
        version.onPreferenceClickListener = this
        clear.onPreferenceClickListener = this
        version.onPreferenceClickListener = this
        ftpUser.onPreferenceClickListener = this
        ftpPass.onPreferenceClickListener = this
        features.onPreferenceClickListener = this
        port.onPreferenceClickListener = this
        service.onPreferenceClickListener = this
        scan.onPreferenceClickListener = this

        ftpUser.onPreferenceChangeListener = this
        ftpPass.onPreferenceChangeListener = this
        features.onPreferenceChangeListener = this
        port.onPreferenceChangeListener = this
        scan.onPreferenceChangeListener = this
        service.onPreferenceChangeListener = this
        initData()
    }

    override fun initData() {
        val title: CharSequence = if (sync.isConnected)
            sync.wifiInfo.ssid?.replace("\"", "") ?: "Unknown"
        else
            "Not connected"

        val summary: CharSequence = if (sync.isConnected) sync.ipAddress else "Not connected"
        wifi.title = "Wifi SSID: $title"
        wifi.summary = summary
        version.summary = BuildConfig.VERSION_NAME
        targetName.summary = manager.targetName
        targetVersion.summary = manager.targetVersion
        ftpUser.summary = manager.ftpUser.takeIf { it?.isNotEmpty() == true } ?: "no user set"
        ftpPass.summary = manager.ftpPass.takeIf { it?.isNotEmpty() == true }?.let { "*********" }
            ?: "no password set"
        ftpUser.dialogTitle = "FTP Username"
        ftpUser.dialogMessage = "Enter the FTP username"
        ftpUser.dialogIcon = ContextCompat.getDrawable(requireContext(), R.mipmap.orb)
        ftpPass.dialogTitle = "FTP Password"
        ftpPass.dialogMessage = "Enter the FTP password"
        ftpPass.dialogIcon = ContextCompat.getDrawable(requireContext(), R.mipmap.orb)
        features.summary = manager.featurePort.title
        port.summary = manager.jbPort.toString()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as? MainContract.View)
    }

    override fun onDetach() {
        super.onDetach()
        mainView = null
    }

    override fun onCleared() {
        mainView?.setSummary("Current Target: none")
        Snackbar.make(requireView(), "Settings cleared", Snackbar.LENGTH_LONG).show()
    }

    override fun onError(e: Throwable) = Unit

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            ftpUser.key -> {
                ftpUser.summary = newValue?.toString()
                true
            }
            ftpPass.key -> {
                ftpPass.summary = "password set"
                true
            }
            features.key -> {
                features.summary = newValue?.toString()
                true
            }
            port.key -> {
                port.summary = newValue?.toString()
                presenter.restart()
                true
            }
            scan.key -> {
                Snackbar.make(requireView(), "Value :Set", Snackbar.LENGTH_SHORT).show()
                true
            }
            service.key -> {
                val value = newValue as Boolean
                if (value) {
                    presenter.start()
                } else{
                    presenter.stop()
                }
                true
            }
            else -> false
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
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
                presenter.clear()
            }
            version.key -> {}
            dev.key -> {}
            ftpUser.key -> {}
            ftpPass.key -> {}
        }
        return true
    }


}