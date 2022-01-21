package nyc.vonley.mi.models.enums

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import nyc.vonley.mi.R
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
enum class Feature(val title: String, @StringRes val id: Int, vararg val ports: Int) : Parcelable {
    NONE("None", R.string.feature_none, 0),
    NETCAT("Netcat", R.string.feature_netcat, 9021, 9020),
    GOLDENHEN("Golden Hen", R.string.feature_goldhen, 9090),
    ORBISAPI("Orbis API", R.string.feature_orbisapi, 6023),
    FTP(
        "FTP",
        R.string.feature_ftp,
        21,
        2121
    );

    companion object {
        fun find(context: Context, id: String): Feature? {
            return values().filter { p -> context.getString(p.id) == id }.firstOrNull()
        }
    }
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

