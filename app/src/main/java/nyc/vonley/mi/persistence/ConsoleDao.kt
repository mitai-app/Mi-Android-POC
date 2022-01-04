package nyc.vonley.mi.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import nyc.vonley.mi.models.Console

@Dao
interface ConsoleDao: IDao<Console, String>{

    @Query("SELECT * FROM Console WHERE wifi = :wifi_ ORDER BY ip ASC")
    fun get(wifi_: String): LiveData<List<Console>>

    @Query("SELECT * FROM Console ORDER BY ip ASC")
    fun getAll(): LiveData<List<Console>>
}