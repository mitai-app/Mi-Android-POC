package io.vonley.mi.ui.main.console.sheets

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.vonley.mi.R
import io.vonley.mi.databinding.FragmentConsoleOptionSheetBinding
import io.vonley.mi.databinding.VhConsoleOptionBinding

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ConsoleOptionSheetFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class ConsoleOptionSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentConsoleOptionSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConsoleOptionSheetBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.layoutManager = GridLayoutManager(context, 2)
       // binding.list.adapter = ItemAdapter(6)
    }



    companion object {

        // TODO: Customize parameters
        fun newInstance(itemCount: Int): ConsoleOptionSheetFragment =
            ConsoleOptionSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}