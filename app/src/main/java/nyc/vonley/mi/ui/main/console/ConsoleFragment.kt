package nyc.vonley.mi.ui.main.console

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.R
import nyc.vonley.mi.databinding.FragmentConsoleBinding
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.ui.main.console.adapters.ConsoleRecyclerAdapter
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsoleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ConsoleFragment : Fragment(), ConsoleContract.View {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var adapter: ConsoleRecyclerAdapter

    @Inject
    lateinit var presenter: ConsoleContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ConsoleRecyclerAdapter()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = FragmentConsoleBinding.inflate(inflater, container, false)
        inflate.consoleRecycler.adapter = adapter
        return inflate.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConsoleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConsoleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onConsolesFound(consoles: List<Console>) {
        adapter.setData(consoles)
    }

    override fun onError(e: Throwable) {
        Log.e("ERROR", "You are shit", e)
    }


}