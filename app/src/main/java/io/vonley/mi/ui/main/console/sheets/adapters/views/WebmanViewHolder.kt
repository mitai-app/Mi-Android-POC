package io.vonley.mi.ui.main.console.sheets.adapters.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.vonley.mi.databinding.VhWebmanOptionBinding
import io.vonley.mi.databinding.ViewWebmanBinding
import io.vonley.mi.di.network.protocols.webman.Game
import io.vonley.mi.di.network.protocols.webman.GameType
import io.vonley.mi.di.network.protocols.webman.WebMan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebmanViewHolder(
    val binding: ViewWebmanBinding,
    override val protocol: WebMan
) :
    RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<WebMan> {

    inner class GameSetAdapter(gameSet: Map<GameType, List<Game>>) : RecyclerView.Adapter<GameSetAdapter.GameSetViewHolder>() {

        private val games = gameSet.values.flatten()

        init {
            notifyDataSetChanged()
        }

        inner class GameSetViewHolder(val binding: VhWebmanOptionBinding) : RecyclerView.ViewHolder(binding.root) {

            fun init(game: Game) {
                Glide.with(binding.webmanGameCover).asBitmap()
                    .load(game.icon).into(binding.webmanGameCover)
                binding.webmanGameTitle.text = game.title
                binding.root.setOnClickListener {
                    val request = protocol.getRequest(game.link)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSetAdapter.GameSetViewHolder {
            return GameSetViewHolder(
                VhWebmanOptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }


        override fun onBindViewHolder(holder: GameSetAdapter.GameSetViewHolder, position: Int) {
            holder.init(games[position])
        }

        override fun getItemCount(): Int {
            return games.size
        }

    }


    override fun init() {


        launch {
            protocol.notify("Webman Attached")
            val games = protocol.searchGames()
            withContext(Dispatchers.Main) {

                binding.webmanRecycler.adapter = GameSetAdapter(games)
            }
        }
    }

}