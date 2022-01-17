package nyc.vonley.mi.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.ConsoleTypeConverter
import nyc.vonley.mi.models.enums.Feature
import nyc.vonley.mi.models.enums.FeaturesConverter
import java.net.Socket
import kotlin.coroutines.CoroutineContext

@Entity
@Parcelize
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
data class Console(
    @PrimaryKey override val ip: String,
    override var name: String,
    override var type: ConsoleType,
    override var features: List<Feature> = emptyList(),
    override var lastKnownReachable: Boolean,
    override var wifi: String
) : Client, Parcelable, CoroutineScope {

    @Ignore
    var socket: Socket? = null

    fun connect() {
        launch {
            socket = Socket()
        }
    }

    @Ignore
    private val job = Job()


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun toString(): String {
        return """
                    \nIP: $ip
                    Name: $name
                    Type: $type
                    Features: $features
                    Reachable: $lastKnownReachable
                """.trimIndent()
    }
}