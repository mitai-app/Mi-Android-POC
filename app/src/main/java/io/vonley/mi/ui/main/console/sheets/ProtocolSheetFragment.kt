package io.vonley.mi.ui.main.console.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.databinding.FragmentConsoleOptionSheetBinding
import io.vonley.mi.ui.main.console.sheets.adapters.ProtocolRecyclerAdapter
import javax.inject.Inject


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ConsoleOptionSheetFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
@AndroidEntryPoint
class ProtocolSheetFragment : BottomSheetDialogFragment(), ProtocolContract.View {

    private var _binding: FragmentConsoleOptionSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var adapter: ProtocolRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsoleOptionSheetBinding.inflate(inflater, container, false)
        binding.list.layoutManager = GridLayoutManager(context, 1)
        binding.list.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       adapter.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    override fun onError(e: Throwable) {

    }
}