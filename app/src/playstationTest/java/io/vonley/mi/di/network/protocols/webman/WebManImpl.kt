package io.vonley.mi.di.network.protocols.webman

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.protocols.common.models.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import kotlin.coroutines.CoroutineContext

class WebManImpl(override val service: PSXService, val manager: AssetManager) : WebMan {

    private val xml = manager.open("webman/games.xml").readBytes().decodeToString()

    private val _liveProcesses = MutableLiveData<List<Process>>()

    private val _processes = arrayListOf<Process>()

    override var attached: Boolean = false

    override var process: Process? = null

    override val processes: List<Process>
        get() = _processes

    override val liveProcesses: LiveData<List<Process>>
        get() = _liveProcesses

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = Job()

    override suspend fun searchGames(): Map<GameType, List<Game>> {
        val document = Jsoup.parse(xml, "", Parser.xmlParser())
        return hashMapOf(
            Pair(GameType.PS3, getPS3Games(document)),
            Pair(GameType.PS2, getPS2Games(document)),
            Pair(GameType.PSP, getPSPGames(document)),
            Pair(GameType.PSX, getPSXGames(document))
        )
    }



}