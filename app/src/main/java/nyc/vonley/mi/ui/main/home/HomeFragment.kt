package nyc.vonley.mi.ui.main.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import nyc.vonley.mi.databinding.FragmentHomeBinding
import nyc.vonley.mi.di.network.MiJBServer
import nyc.vonley.mi.helpers.Voice
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), MiJBServer.MiJbServerListener {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var jb: MiJBServer

    @Inject
    lateinit var voice: Voice

    private lateinit var md: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        md = resources.assets.open("Home.md").readBytes().decodeToString()
        val markwon = Markwon.create(requireContext())
        binding.device.text = "http://${jb.service.sync.ipAddress}:8080"
        markwon.setMarkdown(binding.md, md)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jb.add(this)
    }

    override fun onDeviceConnected(device: MiJBServer.Device) {
        binding.device.text = device.ip
        binding.server.text = device.device
    }

    override fun onLog(string: String) {
        "${binding.logs.text}\n$string\n".also { binding.logs.text = it }
        binding.logs.scrollTo(0, binding.logs.bottom)
    }

    fun dialog(message: String, dialog: DialogInterface.OnClickListener): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("ミ (mi)")
            .setMessage(message)
            .setPositiveButton("OK", dialog).create()
    }

    override fun onJailbreakSucceeded(message: String) {
        dialog(message) { dialog, i ->
            dialog.dismiss()
        }.show()
    }

    override fun onJailbreakFailed(message: String) {
        dialog(message) { dialog, i ->
            dialog.dismiss()
        }.show()
    }

    override fun onPayloadSent() {
        Snackbar.make(requireView(), "Sending payload... please wait....", Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onUnsupported(s: String) {
        "${binding.logs.text}\n$s\n".also { binding.logs.text = it }
        binding.logs.scrollTo(0, binding.logs.bottom)
    }

    override fun onCommand(mi: MiJBServer.Mi<MiJBServer.Mi.Cmd>) {
        "${binding.logs.text}\n${mi.message}\n".also { binding.logs.text = it }
        binding.logs.scrollTo(0, binding.logs.bottom)
    }
}