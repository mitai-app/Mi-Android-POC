package io.vonley.mi.models.enums

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import io.vonley.mi.R
import io.vonley.mi.extensions.fromJson
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
enum class PlatformType : Parcelable {
    UNKNOWN, PS3, PS4
}

@Entity
@Parcelize
enum class Protocol : Parcelable {
    HTTP, SOCKET, FTP, NONE
}

/**
 * We wont scan for netcat, this port will be ignored since its a
 * oneshot jb exploit. Our MiJbServer class will handle the netcat
 * payload
 */
@Entity
@Parcelize
enum class Feature(
    val title: String,
    @StringRes val id: Int,
    val protocol: Protocol,
    vararg val ports: Int
) : Parcelable {
    NONE("None", R.string.feature_none, Protocol.NONE, 0),
    NETCAT("Netcat", R.string.feature_netcat, Protocol.SOCKET,9021, 9020),
    GOLDENHEN("Golden Hen", R.string.feature_goldhen, Protocol.SOCKET,9090),
    ORBISAPI("Orbis API", R.string.feature_orbisapi, Protocol.SOCKET, 6023),
    RPI("Remote Package Installer", R.string.feature_rpi, Protocol.HTTP,12800),
    PS3MAPI("PS3MAPI", R.string.feature_ps3mapi, Protocol.SOCKET,7887),
    CCAPI("CCAPI", R.string.feature_ccapi, Protocol.HTTP,6333),
    WEBMAN("WEBMAN", R.string.feature_webman, Protocol.HTTP,80),
    FTP("FTP", R.string.feature_ftp, Protocol.FTP, 21, 2121);

    companion object {
        fun find(context: Context, id: String): Feature? {
            return values().filter { p -> context.getString(p.id) == id }.firstOrNull()
        }
    }
}

class ProtocolTypeConverter {

    @TypeConverter
    fun toType(value: String): Protocol = enumValueOf(value)

    @TypeConverter
    fun fromType(value: Protocol) =  value.name

}

class ConsoleTypeConverter {

    @TypeConverter
    fun toType(value: Int): PlatformType = enumValues<PlatformType>()[value]

    @TypeConverter
    fun fromType(value: PlatformType) = value.ordinal

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

