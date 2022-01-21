package nyc.vonley.mi.ui.main.console

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.databinding.FragmentConsoleBinding
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.ui.dialogs.MiInputDialog
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.ui.main.console.adapters.ConsoleRecyclerAdapter
import nyc.vonley.mi.ui.main.home.dialog
import javax.inject.Inject

/**
 * Console Fragment to handle connected clients
 */
@AndroidEntryPoint
class ConsoleFragment : Fragment(), ConsoleContract.View {

    private var mainView: MainContract.View? = null

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
        mainView?.setSummary(presenter.getTargetSummary)
        return inflate.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as? MainContract.View)
    }

    override fun onDetach() {
        super.onDetach()
        mainView = null
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onClientsFound(clients: List<Client>) = Unit

    override fun addConsole() {
        MiInputDialog.createDialog("Enter Console IP", "192.168.?.?").show(childFragmentManager, TAG)
    }

    override fun onConsoleAdded() {
        Snackbar.make(requireView(), "Console added!", Snackbar.LENGTH_LONG).show()
    }

    override fun onError(e: Throwable) {
        Log.e("ERROR", "You are shit", e)
    }

    override fun onEmptyDataReceived() {

    }

    override fun onDialogInput(input: String) {
        super.onDialogInput(input)
        presenter.addConsole(input)
    }

    override fun onAlreadyStored() {

    }

    override val TAG: String
        get() = ConsoleFragment::class.java.name


}