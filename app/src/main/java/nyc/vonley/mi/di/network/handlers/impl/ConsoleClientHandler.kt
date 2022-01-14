package nyc.vonley.mi.di.network.handlers.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nyc.vonley.mi.di.network.handlers.base.BaseClientHandler
import nyc.vonley.mi.di.network.listeners.OnConsoleListener
import nyc.vonley.mi.models.Client
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.persistence.ConsoleDao

class ConsoleClientHandler constructor(
    val consoleDao: ConsoleDao
) : BaseClientHandler<OnConsoleListener, List<Console>>(), OnConsoleListener {

    override fun onClientsFound(clients: List<Client>) {
        super.onClientsFound(clients)
        listeners.values.forEach {
            it.onClientsFound(clients)
        }
    }

    override fun onEmptyDataReceived() {

    }

    override fun onAlreadyStored() {

    }

    override val TAG: String = ConsoleClientHandler::class.java.name

    public override fun handle(event: List<Console>): Job {
        Log.e(TAG, "OK")
        return launch {
            try {
                consoleDao.insert(event)
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