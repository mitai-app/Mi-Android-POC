package io.vonley.mi.models

import com.google.gson.annotations.SerializedName

data class Mi<T>(
    @SerializedName("response") val response: String,
    @SerializedName("data") val data: T
) {
    data class Cmd(@SerializedName("cmd") val cmd: String)
}