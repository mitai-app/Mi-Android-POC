package io.vonley.mi.ui.main.console.sheets.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.ViewKlogBinding
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.ui.main.console.sheets.adapters.KLoggingAdapter

class KLogViewHolder(
    val binding: ViewKlogBinding,
    override val protocol: KLog
) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<KLog> {

    private var adapter: KLoggingAdapter? = null

    override fun init() {
        adapter = KLoggingAdapter(protocol)
        binding.recycler.adapter = adapter
        binding.recycler.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }
    }

    override fun cleanup() {
        adapter?.cleanup()
        adapter = null
    }
}