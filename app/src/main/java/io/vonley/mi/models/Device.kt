package io.vonley.mi.models

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Device(
    val device: String,
    val version: String,
    val ip: String
) : Parcelable


val Device.jbPath: String get() {
    return when (version) {
        "5.05" -> "jb/505/"
        "6.72" -> "jb/672/"
        "7.02" -> "jb/702/"
        "7.50",
        "7.51",
        "7.55" -> "jb/75x/"
        "9.00" -> "jb/900/"
        else -> "pages/fail.html"
    }
}
val Device.supported: Boolean get() = when (version) {
    "6.72", "7.02", "7.50", "7.51", "7.55", "9.00" -> true
    else -> false
}