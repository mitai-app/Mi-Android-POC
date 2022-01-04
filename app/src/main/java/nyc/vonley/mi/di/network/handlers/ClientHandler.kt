package nyc.vonley.mi.di.network.handlers

import nyc.vonley.mi.models.Client

interface ClientHandler {

    fun onClientFound(client: Client)

}