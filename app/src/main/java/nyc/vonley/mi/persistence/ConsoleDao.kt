package nyc.vonley.mi.persistence

<<<<<<< HEAD
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
=======
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
>>>>>>> b6ad848beeff89f262b87d4d684f9a420852a922
import nyc.vonley.mi.models.Console

@Dao
interface ConsoleDao: IDao<Console, String>{
<<<<<<< HEAD

    @Query("SELECT * FROM Console WHERE wifi = :wifi_ ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun get(wifi_: String): LiveData<List<Console>>

    @Query("SELECT * FROM Console ORDER BY LENGTH(features) DESC, lastKnownReachable DESC ")
    fun getAll(): LiveData<List<Console>>
=======
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(consoles: List<Console>)
>>>>>>> b6ad848beeff89f262b87d4d684f9a420852a922
}