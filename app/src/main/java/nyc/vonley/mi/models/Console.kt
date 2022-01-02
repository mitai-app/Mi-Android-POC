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
    @PrimaryKey val ip: String,
    var name: String,
    var type: ConsoleType,
    var features: List<Features> = emptyList()
) : Parcelable {
    override fun toString(): String {
        return """
                    IP: $ip
                    Name: $name
                    Type: $type
                    Features: $features
                """.trimIndent()
    }
}