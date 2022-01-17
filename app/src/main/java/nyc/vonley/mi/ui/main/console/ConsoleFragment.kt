package nyc.vonley.mi.ui.main.console

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.databinding.FragmentConsoleBinding
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.ui.main.console.adapters.ConsoleRecyclerAdapter
import javax.inject.Inject

/**
 * Console Fragment to handle connected clients
 */
@AndroidEntryPoint
class ConsoleFragment : Fragment(), ConsoleContract.View {

    @Inject
    lateinit var vm: ConsoleViewModel

    @Inject
    lateinit var adapter: ConsoleRecyclerAdapter

    @Inject
    lateinit var presenter: ConsoleContract.Presenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentConsoleBinding.inflate(inflater, container, false)
        vm.consoles.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapter.setData(it)
            } else {

            }
        })
        inflate.consoleRecycler.adapter = adapter
        return inflate.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onClientsFound(clients: List<Client>) = Unit

    override fun onError(e: Throwable) {
        Log.e("ERROR", "You are shit", e)
    }

    override fun onEmptyDataReceived() {

    }

    override fun onAlreadyStored() {

    }

    override val TAG: String
        get() = ConsoleFragment::class.java.name


}