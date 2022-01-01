package nyc.vonley.mi.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import nyc.vonley.mi.enums.ConsoleType

@Entity
@Parcelize
data class Console(
    @PrimaryKey val ip: String,
    var name: String,
    var type: ConsoleType,
): Parcelable {

}