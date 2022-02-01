package io.vonley.mi.ui.main.console.sheets.adapters.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.VhConsoleOptionBinding
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import kotlinx.coroutines.launch

class PS3MAPIViewHolder(
    val binding: VhConsoleOptionBinding,
    override val protocol: PS3MAPI
) : RecyclerView.ViewHolder(binding.root),
    ViewHolderProtocol<PS3MAPI> {

    override fun init() {
        launch {
            protocol.connect()
            protocol.notify("PS3MAPI Attached")
        }
    }

}