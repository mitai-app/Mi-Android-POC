package nyc.vonley.mi.ui.main.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import nyc.vonley.mi.databinding.FragmentHomeBinding
import nyc.vonley.mi.di.network.MiJBServer
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), MiJBServer.MiJbServerListener {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var jb: MiJBServer

    private lateinit var md: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        md = resources.assets.open("Home.md").readBytes().decodeToString()
        val markwon = Markwon.create(requireContext())
        binding.device.text = "Visit: http://${jb.service.sync.ipAddress}:8080"
        markwon.setMarkdown(binding.logs, md)
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
    }

    override fun onFinishedJb() {

    }

    override fun onPayloadSent() {

    }

    override fun onUnsupported(s: String) {
        "${binding.logs.text}\n$s\n".also { binding.logs.text = it }
    }
}