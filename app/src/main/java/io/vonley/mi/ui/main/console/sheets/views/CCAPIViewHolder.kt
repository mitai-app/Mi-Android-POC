package io.vonley.mi.ui.main.console.sheets.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.VhConsoleOptionBinding
import io.vonley.mi.databinding.ViewCcapiBinding
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import kotlinx.coroutines.launch

class CCAPIViewHolder(
    val binding: ViewCcapiBinding,
    override val protocol: CCAPI
) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<CCAPI> {

    override fun init() {
        binding.subheader.text = "This feature is available!"
    }
}