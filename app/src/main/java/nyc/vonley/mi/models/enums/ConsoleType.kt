package nyc.vonley.mi.models.enums

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import nyc.vonley.mi.extensions.fromJson

@Entity
@Parcelize
enum class ConsoleType : Parcelable {
    UNKNOWN, PS3, PS4
}

@Entity
@Parcelize
enum class Features(val title: String, vararg val ports: Int) : Parcelable {
    NONE("None", 0),
    GOLDENHEN("Golden Hen", 9090),
    ORBISAPI("Orbis API", 6023),
    FTP(
        "FTP",
        21,
        2121
    )
}


class ConsoleTypeConverter {

    @TypeConverter
    fun toType(value: Int): ConsoleType = enumValues<ConsoleType>()[value]

    @TypeConverter
    fun fromType(value: ConsoleType) = value.ordinal

}

class FeaturesConverter {

    @TypeConverter
    fun toType(features: String): List<Features> {
        return GsonBuilder().create().fromJson<List<Int>>(features)
            .map { enumValues<Features>()[it] }
    }

    @TypeConverter
    fun fromType(values: List<Features>): String {
        val transform: (Features) -> Int = { it.ordinal }
        val map = values.map(transform)
        return GsonBuilder().create().toJson(map)
    }

}

