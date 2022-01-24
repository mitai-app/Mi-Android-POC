package nyc.vonley.mi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.models.enums.ConsoleTypeConverter
import nyc.vonley.mi.models.enums.FeaturesConverter

@Database(
    entities = [
        Console::class
    ], version = 2, exportSchema = true
)
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
}