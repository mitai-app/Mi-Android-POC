package io.vonley.mi.ui.main.console.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.vonley.mi.databinding.VhConsoleOptionBinding
import io.vonley.mi.databinding.VhWebmanOptionBinding
import io.vonley.mi.databinding.ViewWebmanBinding
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.di.network.protocols.webman.Game
import io.vonley.mi.di.network.protocols.webman.GameType
import io.vonley.mi.di.network.protocols.webman.WebMan
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.Feature.*
import io.vonley.mi.ui.main.MainContract
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject


class ConsoleOptionRecyclerAdapter @Inject constructor(
    val view: MainContract.View,
    val ps3mapi: PS3MAPI,
    val ccapi: CCAPI,
    val webman: WebMan,
    val service: PSXService
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Observer<List<Feature>> {

    private var services: List<PSXProtocol> = emptyList()

    init {
        service.features.observeForever(this)
        service.initialize()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = when (viewType) {
            Feature.WEBMAN.ordinal -> ConsoleWebmanViewHolder(
                ViewWebmanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), webman
            )
            Feature.PS3MAPI.ordinal -> ConsolePS3MAPIViewHolder(
                VhConsoleOptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), ps3mapi
            )
            Feature.CCAPI.ordinal -> ConsoleCCAPIViewHolder(
                VhConsoleOptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), ccapi
            )
            else -> throw Exception("Invalid View")
        }
        return view
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val protocol = this.services[position]
        val holder = when (protocol.feature) {
            WEBMAN -> (holder as? ConsoleWebmanViewHolder)
            PS3MAPI -> (holder as? ConsolePS3MAPIViewHolder)
            CCAPI -> (holder as? ConsoleCCAPIViewHolder)
            else -> null
        }
        holder?.init()
    }

    override fun getItemViewType(position: Int): Int = this.services[position].feature.ordinal


    override fun getItemCount(): Int {
        return this.services.size
    }

    interface ViewHolderProtocol<S : PSXProtocol> : CoroutineScope {
        val protocol: S

        fun init()

        private val job get() = Job()

        override val coroutineContext get() = Dispatchers.IO + job
    }

    inner class ConsoleWebmanViewHolder(
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
                        protocol.getRequest(game.link, object: Callback {

                            override fun onFailure(call: Call, e: IOException) {

                            }

                            override fun onResponse(call: Call, response: Response) {
                                Log.e(protocol.TAG, "SENT")
                            }

                        })
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSetAdapter.GameSetViewHolder {
                return GameSetViewHolder(VhWebmanOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    inner class ConsolePS3MAPIViewHolder(
        val binding: VhConsoleOptionBinding,
        override val protocol: PS3MAPI
    ) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<PS3MAPI> {

        override fun init() {
            launch {
                protocol.connect()
                protocol.notify("PS3MAPI Attached")
            }
        }

    }

    inner class ConsoleCCAPIViewHolder(
        val binding: VhConsoleOptionBinding,
        override val protocol: CCAPI
    ) : RecyclerView.ViewHolder(binding.root), ViewHolderProtocol<CCAPI> {

        override fun init() {
            launch {
                protocol.notify(CCAPI.NotifyIcon.INFO, "Attached to CCAPI")
            }
        }
    }

    override fun onChanged(t: List<Feature>?) {
        t?.let { features ->
            val target = service.target
            if (target != null) {
                services = features.mapNotNull { f ->
                    when (f) {
                        ps3mapi.feature -> ps3mapi
                        ccapi.feature -> ccapi
                        webman.feature -> webman
                        else -> null
                    }
                }
                notifyDataSetChanged()
            }
        } ?: run {

        }
    }

}