package io.vonley.mi.di.network.protocols.webman

import android.content.res.AssetManager
import io.vonley.mi.di.network.PSXService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import kotlin.coroutines.CoroutineContext

class WebManImplTest(override val service: PSXService, val manager: AssetManager) : WebMan {

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job
    private val xml = manager.open("webman/games.xml").readBytes().decodeToString()

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