package io.vonley.mi.di.network.callbacks

import io.vonley.mi.models.Payload

interface PayloadCallback {
    fun onFinished()
    fun onWriting(payload: Payload)
    fun onSent(payload: Payload)
    fun onPayloadFailed(payload: Payload)
}