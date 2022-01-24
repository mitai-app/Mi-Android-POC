package io.vonley.mi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.ConsoleTypeConverter
import io.vonley.mi.models.enums.FeaturesConverter

@Database(
    entities = [
        Console::class
    ], version = 2, exportSchema = true
)
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
}