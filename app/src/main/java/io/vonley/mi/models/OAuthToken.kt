package io.vonley.mi.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OAuthToken(
    val token_type: String,
    val access_token: String,
    val refresh_token: String,
    val expires: Long
) : Parcelable