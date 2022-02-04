package io.vonley.mi.ui.main.console.sheets.adapters

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.VhKlogBinding
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.extensions.e

class KLoggingAdapter(val protocol: KLog) : RecyclerView.Adapter<KLoggingAdapter.ViewHolder>(),
    KLog.KLogger {

    private val logs = arrayListOf<Spannable>()

    init {
        protocol.attach(this)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.scrollToPosition(itemCount)
    }

    inner class ViewHolder(val binding: VhKlogBinding) : RecyclerView.ViewHolder(binding.root) {

        fun log(string: Spannable) {
            binding.log.setText(string, TextView.BufferType.SPANNABLE)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(VhKlogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.log(logs[position])
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onLog(string: Spannable) {
        logs.add(string)
        notifyDataSetChanged()
    }

    fun cleanup() {
        protocol.detach(this)
        "KLOG detached from view".e(this.javaClass.name)
    }

}
