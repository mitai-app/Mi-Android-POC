package io.vonley.mi.di.network.handlers.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.vonley.mi.di.network.handlers.base.BaseClientHandler
import io.vonley.mi.di.network.listeners.OnConsoleListener
import io.vonley.mi.models.Client
import io.vonley.mi.models.Console
import io.vonley.mi.persistence.ConsoleDao

/**
 * If you made it here congratulations. What is this ConsoleClientHandler?
 * Well *Slaps top of the class* this bad boy here is the overall global
 * listeners updates the entire app, who listens for these newly found consoles.
 * However the best part about this is that we are able to save the consoles into
 * our sqlite console dao.
 * So there isn't really much to do here.
 */
class ConsoleClientHandler constructor(
    val consoleDao: ConsoleDao
) : BaseClientHandler<OnConsoleListener, List<Console>>(), OnConsoleListener {

    override fun onClientsFound(clients: List<Client>) {
        super.onClientsFound(clients)
        listeners.values.forEach {
            it.onClientsFound(clients)
        }
    }

    override fun onEmptyDataReceived() = Unit

    override fun onAlreadyStored() = Unit

    override val TAG: String = ConsoleClientHandler::class.java.name

    public override fun handle(event: List<Console>): Job {
        return launch {
            try {
                event.onEach { console ->
                    if (consoleDao.exists(console.ip)) {
                        consoleDao.update(
                            console.ip,
                            console.type,
                            console.features,
                            console.lastKnownReachable,
                            console.wifi
                        )
                    } else {
                        consoleDao.insert(console)
                    }
                }
                withContext(Dispatchers.Main) {
                    if (event.isNotEmpty()) {
                        onClientsFound(event)
                    } else {
                        onEmptyDataReceived()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}