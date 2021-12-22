package nyc.vonley.mi.persistence

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface IDao<Model, Identity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(models: List<Model>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg models: Model): LongArray

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(model: Model)

    @Delete
    suspend fun delete(model: Model)
}