package io.vonley.mi.ui.main.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.TextViewBinding

class TextViewAdapter : RecyclerView.Adapter<TextViewAdapter.TextViewHolder>() {

    private val logs = ArrayList<String>()

    inner class TextViewHolder(val binding: TextViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val inflate =
            TextViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TextViewHolder(inflate)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.scrollToPosition(itemCount)
    }
    fun add(string: String) {
        logs += string
        notifyDataSetChanged()
    }

    fun clear() {
        logs.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.binding.root.text = logs[position]
    }

    override fun getItemCount(): Int {
        return logs.size
    }
}