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

/**
 * We wont scan for netcat, this port will be ignored since its a
 * oneshot jb exploit. Our MiJbServer class will handle the netcat
 * payload
 */
@Entity
@Parcelize
enum class Feature(val title: String, vararg val ports: Int) : Parcelable {
    NONE("None", 0),
    NETCAT("Netcat", 9021, 9020),
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
    fun toType(features: String): List<Feature> {
        return GsonBuilder().create().fromJson<List<Int>>(features)
            .map { enumValues<Feature>()[it] }
    }

    @TypeConverter
    fun fromType(values: List<Feature>): String {
        val transform: (Feature) -> Int = { it.ordinal }
        val map = values.map(transform)
        return GsonBuilder().create().toJson(map)
    }

}

