package io.vonley.mi.ui.main.console.sheets.views

import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.ViewWebmanBinding
import io.vonley.mi.di.network.protocols.webman.Webman
import io.vonley.mi.ui.main.console.sheets.adapters.WebmanGameSetAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebmanViewHolder(
    val binding: ViewWebmanBinding,
    override val protocol: Webman
) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<Webman> {


    override fun init() {
        launch {
            val games = protocol.searchGames()
            withContext(Dispatchers.Main) {
                binding.webmanRecycler.adapter = WebmanGameSetAdapter(games, protocol)
            }
        }
    }

}