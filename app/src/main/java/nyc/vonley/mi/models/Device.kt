package nyc.vonley.mi.models

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