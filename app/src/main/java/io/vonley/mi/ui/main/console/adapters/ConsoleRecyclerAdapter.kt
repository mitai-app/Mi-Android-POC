package io.vonley.mi.ui.main.console.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.vonley.mi.R
import io.vonley.mi.databinding.VhConsoleBinding
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.models.Client
import io.vonley.mi.models.Console
import io.vonley.mi.models.activeFeatures
import io.vonley.mi.models.featureString
import io.vonley.mi.ui.main.MainContract
import io.vonley.mi.ui.main.console.sheets.ProtocolSheetFragment
import javax.inject.Inject

class ConsoleRecyclerAdapter @Inject constructor(
    val view: MainContract.View,
    val sync: SyncService,
    val sheet: ProtocolSheetFragment,
    val manager: FragmentManager
) : RecyclerView.Adapter<ConsoleRecyclerAdapter.ConsoleViewHolder>() {

    private var consoles = emptyList<Console>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        return ConsoleViewHolder(
            VhConsoleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        holder.setConsole(consoles[position])
    }

    override fun getItemCount(): Int {
        return this.consoles.size
    }

    fun setData(consoles: List<Console>) {
        this.consoles = consoles
        notifyDataSetChanged()
    }

    inner class ConsoleViewHolder(val binding: VhConsoleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var client: Client? = null

        fun setTarget(console: Client) {
            this.client = console
            sync.setTarget(console)
            view.setSummary("Current Target: ${console.name}, w/ ${console.featureString}")
            Toast.makeText(itemView.context, "Target set!", Toast.LENGTH_SHORT).show()
            sheet.show(manager, sheet.tag)
        }

        fun setConsole(console: Client) {
            this.client = console
            val headers = if (console.name == console.ip) console.ip else "${console.name} - ${console.ip}"
            val colorInt = if (console.pinned) {
                R.color.material_red
            } else {
                R.color.grey_darker
            }
            val color = ContextCompat.getColorStateList(itemView.context, colorInt);

            binding.vhConsoleNickname.setTextColor(color)
            binding.vhConsoleImg.imageTintList = color
            binding.vhConsoleNickname.text = headers
            binding.vhConsoleIp.text =
                console.featureString.takeIf { it.isNotEmpty() } ?: "Incompatible"
            binding.root.setOnClickListener {
                if (console.activeFeatures.isNotEmpty()) {
                    setTarget(console)
                } else {
                    MaterialAlertDialogBuilder(it.context)
                        .setTitle("Warning")
                        .setMessage("This device does not have any port opens it seems, would you still like to connect?")
                        .setPositiveButton("Connect") { dialog, which ->
                            setTarget(console)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }.show()
                }
            }
        }
    }

}