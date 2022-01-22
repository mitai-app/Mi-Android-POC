package nyc.vonley.mi.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.R
import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.databinding.ActivityMainBinding
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.ui.main.console.ConsoleContract
import nyc.vonley.mi.ui.main.ftp.FTPContract
import nyc.vonley.mi.ui.main.home.HomeContract
import nyc.vonley.mi.ui.main.payload.PayloadContract
import java.io.File
import javax.inject.Inject

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainContract.View,
    NavController.OnDestinationChangedListener {

    @Inject
    lateinit var presenter: MainContract.Presenter

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private val navController: NavController
        get() {
            return navHostFragment.navController
        }


    private val current get() = navHostFragment.childFragmentManager.primaryNavigationFragment
    private fun <T : BaseContract.View> currentView(): T? = current as? T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)
        this.navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        navController.addOnDestinationChangedListener(this)
        presenter.init()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }


    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        binding.fragmentContainer.postDelayed({
            //hideSystemUI()
        }, IMMERSIVE_FLAG_TIMEOUT)
    }


    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.fragmentContainer).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onConsolesFound(consoles: List<Console>) = Unit

    override fun setTitle(title: String?) {
        binding.fakeToolbarTitle.text = title ?: return
    }

    override fun setSummary(summary: String?) {
        binding.fakeToolbarSummary.text = summary ?: return
        binding.fakeToolbarSummary.isSelected = true
    }

    override fun onError(e: Throwable) = Unit

    override fun onDialogCanceled() {
        super.onDialogCanceled()
        currentView<BaseContract.View>()?.onDialogCanceled()
    }

    override fun onDialogInput(input: String) {
        super.onDialogInput(input)
        currentView<BaseContract.View>()?.onDialogInput(input)
    }


    companion object {

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    private val onPayloadClick: View.OnClickListener = View.OnClickListener {
        currentView<PayloadContract.View>()?.open()
    }

    private val onFTPClick: View.OnClickListener = View.OnClickListener {
        currentView<FTPContract.View>()?.open()
    }

    private val onConsoleClick: View.OnClickListener = View.OnClickListener {
        currentView<ConsoleContract.View>()?.addConsole()
    }

    private val onHomeClick: View.OnClickListener = View.OnClickListener {
        currentView<HomeContract.View>()?.openInfoDialog()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val i = when (destination.id) {
            R.id.fragment_payload -> {
                binding.fab.setOnClickListener(onPayloadClick)
                binding.fab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.icon_svg_upload_two
                    )
                )
                View.VISIBLE
            }
            R.id.fragment_ftp -> {
                binding.fab.setOnClickListener(onFTPClick)
                binding.fab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_svg_upload
                    )
                )
                View.VISIBLE
            }
            R.id.fragment_console -> {
                binding.fab.setOnClickListener(onConsoleClick)
                binding.fab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.icon_svg_add
                    )
                )
                View.VISIBLE
            }
            R.id.fragment_home -> {
                binding.fab.setOnClickListener(onHomeClick)
                binding.fab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.icon_svg_info
                    )
                )
                View.VISIBLE
            }
            else -> View.GONE
        }
        binding.fab.visibility = i
    }
}