package nyc.vonley.mi.ui.main.payload.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nyc.vonley.mi.databinding.VhPayloadBinding

class PayloadAdapter : RecyclerView.Adapter<PayloadAdapter.PayloadHolder>() {

    data class Payload(val name: String, val data: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Payload) return false

            if (!data.contentEquals(other.data)) return false
            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    internal val payloads = ArrayList<Payload>()

    inner class PayloadHolder(val binding: VhPayloadBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayloadHolder {
        return PayloadHolder(VhPayloadBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PayloadHolder, position: Int) {
        holder.binding.vhPayloadFile.text = payloads[position].name
        holder.binding.vhPayloadInfo.text = "Bytes: ${payloads[position].data.size}"
    }

    override fun getItemCount(): Int {
        return payloads.size
    }

    fun add(payload: Payload){
        if(!payloads.contains(payload)) {
            payloads.add(payload)
            notifyDataSetChanged()
        }
    }

    fun clear(){
        payloads.clear()
        notifyDataSetChanged()
    }


}
