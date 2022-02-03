package io.vonley.mi.models.enums

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import io.vonley.mi.R
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.extensions.fromJson
import io.vonley.mi.models.Client
import kotlinx.android.parcel.Parcelize
import okhttp3.Request

@Entity
@Parcelize
enum class PlatformType(vararg val features: Feature) : Parcelable {
    UNKNOWN(
        Feature.FTP
    ),
    PS3(
        *arrayOf(
            Feature.PS3MAPI,
            Feature.WEBMAN,
            Feature.CCAPI
        )
    ),
    PS4(
        *arrayOf(
            Feature.GOLDENHEN,
            Feature.NETCAT,
            Feature.ORBISAPI,
            Feature.RPI
        )
    )


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
    NETCAT("Netcat", R.string.feature_netcat, Protocol.SOCKET, 9021, 9020),
    GOLDENHEN("Golden Hen", R.string.feature_goldhen, Protocol.SOCKET, 9090),
    ORBISAPI("Orbis API", R.string.feature_orbisapi, Protocol.SOCKET, 6023),
    RPI("Remote Package Installer", R.string.feature_rpi, Protocol.HTTP, 12800),
    PS3MAPI("PS3MAPI", R.string.feature_ps3mapi, Protocol.SOCKET, 7887),
    CCAPI("CCAPI", R.string.feature_ccapi, Protocol.HTTP, 6333),
    WEBMAN("WEBMAN", R.string.feature_webman, Protocol.HTTP, 80),
    FTP("FTP", R.string.feature_ftp, Protocol.FTP, 21, 2121);

    fun validate(client: Client, service: SyncService): Boolean {
        fun req(url: String): String? {
            val req = Request.Builder()
                .url(url)
                .get()
                .build()
            val execute = service.client.newCall(req)
            val response = execute.execute()
            val s = response.body?.string()
            return s
        }

        return when (this) {
            WEBMAN -> req("http://${client.ip}:${ports.first()}/index.ps3")?.let { s ->
                s.lowercase().contains("ps3mapi") || s.lowercase()
                    .contains("webman") || s.lowercase().contains("dex") ||
                        s.lowercase().contains("d-rex") || s.lowercase()
                    .contains("cex") || s.lowercase()
                    .contains("rebug") ||
                        s.lowercase().contains("rsx")
            }?:false
            CCAPI -> req("http://${client.ip}:${ports.first()}/ccapi")?.let { s ->

                true
            }?:false
            else -> true
        }

    }

    companion object {
        fun find(context: Context, id: String): Feature? {
            return values().firstOrNull { p -> context.getString(p.id) == id }
        }

        /**
         * These fields are for client
         * Due to GoldenHen & NetCat being the payload sender,
         * we want to be extract careful what we send there.
         * Goldenhen Bin Uploader stops working after a while
         * NetCat
         */
        //arrayOf(ORBISAPI, RPI, PS3MAPI, CCAPI, WEBMAN, FTP)
        val stableFeatures: Array<Feature> =
            Feature.values().filterNot { p -> p in arrayOf(NONE, NETCAT, GOLDENHEN) }.toTypedArray()

        /**
         * These are stable sockets that are allowed to be opened for however long
         */
        val allowedToOpen: Array<Feature> = arrayOf(PS3MAPI, CCAPI, WEBMAN, ORBISAPI)
    }
}

class ProtocolTypeConverter {

    @TypeConverter
    fun toType(value: String): Protocol = enumValueOf(value)

    @TypeConverter
    fun fromType(value: Protocol) = value.name

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

