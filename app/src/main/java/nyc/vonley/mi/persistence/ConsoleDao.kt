package nyc.vonley.mi.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleType
import nyc.vonley.mi.models.enums.Feature

@Dao
interface ConsoleDao : IDao<Console, String> {

    @Query("SELECT * FROM Console WHERE (wifi = :wifi_ AND LENGTH(features) > 3) OR (name IS NOT ip) ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun get(wifi_: String): LiveData<List<Console>>

    @Query("SELECT * FROM Console WHERE LENGTH(features) > 3 ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun getAll(): LiveData<List<Console>>

    @Query("SELECT EXISTS(SELECT * FROM Console WHERE ip = :ip_)")
    suspend fun exists(ip_: String): Boolean

    @Query("UPDATE Console SET name = :name_ WHERE ip = :ip_")
    suspend fun updateNickName(ip_: String, name_: String)


    @Query("INSERT INTO Console VALUES(:ip_, :name_, :type_, :features_, :lastKnown_, :wifi_)")
    suspend fun add(
        ip_: String,
        name_: String,
        type_: ConsoleType,
        features_: List<Feature>,
        lastKnown_: Boolean,
        wifi_: String
    )


    @Query("UPDATE Console SET type = :type_, features = :features_, lastKnownReachable = :lastKnown_, wifi = :wifi_ WHERE ip = :ip_")
    suspend fun update(
        ip_: String,
        type_: ConsoleType,
        features_: List<Feature>,
        lastKnown_: Boolean,
        wifi_: String
    )

}