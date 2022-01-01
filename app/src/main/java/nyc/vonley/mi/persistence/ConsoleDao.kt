package nyc.vonley.mi.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import nyc.vonley.mi.models.Console

@Dao
interface ConsoleDao: IDao<Console, String>{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(consoles: List<Console>)
}