package io.vonley.mi.ui.main.console.sheets.adapters.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.VhConsoleOptionBinding
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import kotlinx.coroutines.launch

class CCAPIViewHolder(
    val binding: VhConsoleOptionBinding,
    override val protocol: CCAPI
) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<CCAPI> {

    override fun init() {
        launch {
            protocol.notify(CCAPI.NotifyIcon.INFO, "Attached to CCAPI")
        }
    }
}