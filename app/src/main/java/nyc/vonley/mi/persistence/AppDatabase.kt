package nyc.vonley.mi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import nyc.vonley.mi.models.Console

@Database(
    entities = [
        Console::class
    ], version = 1, exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun consoleDao(): ConsoleDao

}