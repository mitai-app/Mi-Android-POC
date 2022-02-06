package io.vonley.mi.ui.main.payload.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import io.vonley.mi.R
import io.vonley.mi.databinding.VhPayloadBinding
import io.vonley.mi.models.Payload
import kotlin.coroutines.CoroutineContext

class PayloadAdapter : RecyclerView.Adapter<PayloadAdapter.PayloadHolder>(), CoroutineScope {

    internal val payloads = ArrayList<Payload>()

    inner class PayloadHolder(val binding: VhPayloadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayloadHolder {
        return PayloadHolder(
            VhPayloadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PayloadHolder, position: Int) {
        val payload = payloads[position]
        holder.binding.vhPayloadFile.text = payload.name
        holder.binding.vhPayloadInfo.text = "Bytes: ${payload.data.size}"
        val color = when (payload.status) {
            -1 -> R.color.material_red
            0 -> R.color.grey_darker
            1 -> R.color.green
            else -> R.color.black
        }
        holder.binding.vhPayloadImg.imageTintList =
            ContextCompat.getColorStateList(holder.binding.root.context, color)
    }

    override fun getItemCount(): Int {
        return payloads.size
    }

    fun update(payload: Payload){
        val indexOf = payloads.indexOf(payload)
        payloads[indexOf] = payload
        notifyItemChanged(indexOf)
    }

    fun add(payload: Payload) {
        if (!payloads.contains(payload)) {
            payloads.add(payload)
            notifyItemInserted(payloads.size)
        }
    }

    fun clear() {
        launch {
            delay(2000)
            withContext(Dispatchers.Main){
                val toSet = payloads.filter { p -> p.status > 0 }.toSet()
                toSet.onEach {
                    val pos = payloads.indexOf(it)
                    payloads.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }
    }

    fun remove(position: Int) {
        launch {
            delay(1000)
            withContext(Dispatchers.Main){
                payloads.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

}
