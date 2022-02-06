package io.vonley.mi.ui.main.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.vonley.mi.BuildConfig
import io.vonley.mi.databinding.FragmentHomeBinding
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.intents.ver
import io.vonley.mi.models.Device
import io.vonley.mi.models.Mi
import io.vonley.mi.ui.main.home.adapters.TextViewAdapter
import io.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), HomeContract.View {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var md: String

    @Inject
    lateinit var presenter: HomeContract.Presenter

    @Inject
    @SharedPreferenceStorage
    lateinit var manager: SharedPreferenceManager

    lateinit var adapter: TextViewAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        md = resources.assets.open("Home.md").readBytes().decodeToString()
        val markwon = Markwon.create(requireContext())
        markwon.setMarkdown(binding.md, md)
        adapter = TextViewAdapter()
        binding.logRecycler.adapter = adapter
        binding.logRecycler.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.init()
    }

    override fun onDeviceConnected(device: Device) {
        binding.device.text = device.ip
        binding.server.text = device.device
    }

    override fun onLog(string: String) {
        adapter.add(string)
    }

    override fun onJailbreakSucceeded(message: String) {
        dialog(
            "$message\nPssst... Turn on FTP in GoldenHen to auto detect the PS4!",
            "OK"
        ) { dialog, _ ->
            dialog.dismiss()
        }.create().show()
    }

    override fun onJailbreakFailed(message: String) {
        dialog(message, "OK") { dialog, _ ->
            dialog.dismiss()
        }.create().show()
    }

    override fun onDestroyView() {
        presenter.cleanup()
        _binding = null
        super.onDestroyView()
    }

    override fun onPayloadSent(msg: String?) {
        Snackbar.make(requireView(), "Sending payload... please wait....", Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onUnsupported(s: String) {
        adapter.add(s)
    }

    override fun onCommand(mi: Mi<Mi.Cmd>) {
        adapter.add(mi.response)
    }

    override fun onSendPayloadAttempt(attempt: Int) = Unit

    private fun setUpdateIfAvailable() {
        manager.update?.let { meta ->
            if (meta.version.ver > BuildConfig.VERSION_NAME.ver) {
                binding.logs.text = "New update: ${meta.version}\n${meta.changes}\n${meta.build}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setUpdateIfAvailable()
    }

    override fun init(ip: String) {
        binding.device.text = "http://$ip"
        setUpdateIfAvailable()
    }

    override fun openInfoDialog() {
        val message =
            "Thank you for downloading Mi. Start by typing (${binding.device.text}) of this device into your ps4 browser and start the jailbreak process. When the Jailbreak process is complete Goldhen v2.0b2 will be loaded. In order to upload bin files please be sure to go onto your ps4 settings page -> golden hen -> and enable \"BinLoader Server\", likewise to use FTP Feature.\n\nHINT: ENABLE FTP ON YOUR PS4 SO THAT MI CAN DISCOVER YOUR DEVICE AUTOMATICALLY."
        dialog(message, "Close") { dialog, _ -> dialog.dismiss() }.create().show()
    }

    override fun onError(e: Throwable) = Unit

}


fun Fragment.dialog(
    message: String,
    positive: String,
    dialog: DialogInterface.OnClickListener
): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle("ミ (mi)")
        .setMessage(message)
        .setPositiveButton(positive, dialog)
}