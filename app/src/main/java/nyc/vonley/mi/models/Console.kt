package nyc.vonley.mi.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.ConsoleTypeConverter
import nyc.vonley.mi.models.enums.Features
import nyc.vonley.mi.models.enums.FeaturesConverter

@Entity
@Parcelize
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
data class Console(
    @PrimaryKey override val ip: String,
    override var name: String,
    override var type: ConsoleType,
    override var features: List<Features> = emptyList(),
    override var lastKnownReachable: Boolean,
    override var wifi: String,
) : Client, Parcelable {


    override fun toString(): String {
        return """
                    IP: $ip
                    Name: $name
                    Type: $type
                    Features: $features
                    Reachable: $lastKnownReachable
                """.trimIndent()
    }
}