package nyc.vonley.mi.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import nyc.vonley.mi.models.Console

@Dao
interface ConsoleDao: IDao<Console, String>{

    @Query("SELECT * FROM Console WHERE wifi = :wifi_ AND LENGTH(features) > 3 ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun get(wifi_: String): LiveData<List<Console>>

    @Query("SELECT * FROM Console WHERE LENGTH(features) > 3 ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun getAll(): LiveData<List<Console>>

}