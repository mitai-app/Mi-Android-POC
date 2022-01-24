package io.vonley.mi.di.network.listeners

import android.util.Log
import io.vonley.mi.models.Client

interface OnClientListener {

    val TAG: String

    fun onClientsFound(clients: List<Client>) {
        Log.e(TAG, "onClientsFound: # Clients Found: ${clients.size}")
    }

    fun onConnectionOpen(client: Client) {
        Log.e(TAG, "onConnectionOpen")
    }

    fun onConnectionClosing(client: Client) {
        Log.e(TAG, "onConnectionClosing")
    }

    fun onConnectionClosed(client: Client) {
        Log.e(TAG, "onConnectionClosed")
    }

    fun onConnectionFailed(client: Client) {
        Log.e(TAG, "onConnectionFailed")
    }

    fun onAuthenticated(client: Client) {
        Log.e(TAG, "onAuthenticated")
    }

    fun onRejected(client: Client) {
        Log.e(TAG, "onRejected")
    }

}
