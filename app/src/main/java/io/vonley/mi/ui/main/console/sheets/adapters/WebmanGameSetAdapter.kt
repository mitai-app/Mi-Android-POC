package io.vonley.mi.ui.main.console.sheets.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.vonley.mi.databinding.VhWebmanOptionBinding
import io.vonley.mi.di.network.protocols.webman.models.Game
import io.vonley.mi.di.network.protocols.webman.models.GameType
import io.vonley.mi.di.network.protocols.webman.Webman
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class WebmanGameSetAdapter(
    set: Map<GameType, List<Game>>,
    private val protocol: Webman,
) : RecyclerView.Adapter<WebmanGameSetAdapter.GameSetViewHolder>(), CoroutineScope {

    private val games = set.values.flatten()

    init {
        notifyDataSetChanged()
    }

    inner class GameSetViewHolder(val binding: VhWebmanOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun init(game: Game) {
            Glide.with(binding.webmanGameCover).asBitmap()
                .load(game.icon).into(binding.webmanGameCover)
            binding.webmanGameTitle.text = game.title
            binding.root.setOnClickListener {
                launch {
                    val request = protocol.play(game)

                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WebmanGameSetAdapter.GameSetViewHolder {
        return GameSetViewHolder(
            VhWebmanOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: WebmanGameSetAdapter.GameSetViewHolder, position: Int) {
        holder.init(games[position])
    }

    override fun getItemCount(): Int {
        return games.size
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

}