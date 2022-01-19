package nyc.vonley.mi.ui.main.home

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
import nyc.vonley.mi.databinding.FragmentHomeBinding
import nyc.vonley.mi.models.Device
import nyc.vonley.mi.models.Mi
import nyc.vonley.mi.ui.main.home.adapters.TextViewAdapter
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), HomeContract.View {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var md: String

    @Inject
    lateinit var presenter: HomeContract.Presenter

    lateinit var adapter: TextViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        md = resources.assets.open("Home.md").readBytes().decodeToString()
        val markwon = Markwon.create(requireContext())
        markwon.setMarkdown(binding.md, md)
        adapter = TextViewAdapter()
        binding.logRecycler.adapter = adapter
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
        dialog("$message\nPssst... Turn on FTP in GoldenHen to auto detect the PS4!", "OK") { dialog, i ->
            dialog.dismiss()
        }.create().show()
    }

    override fun onJailbreakFailed(message: String) {
        dialog(message, "OK") { dialog, i ->
            dialog.dismiss()
        }.create().show()
    }

    override fun onDestroy() {
        presenter.cleanup()
        super.onDestroy()
    }

    override fun onPayloadSent() {
        Snackbar.make(requireView(), "Sending payload... please wait....", Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onUnsupported(s: String) {
        adapter.add(s)
    }

    override fun onCommand(mi: Mi<Mi.Cmd>) {
        adapter.add(mi.response)
    }

    override fun init(ip: String) {
        binding.device.text = "http://$ip:8080"
    }

    override fun onError(e: Throwable) {

    }


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