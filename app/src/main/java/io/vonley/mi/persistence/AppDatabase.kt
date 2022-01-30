package io.vonley.mi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.vonley.mi.models.Console
import io.vonley.mi.models.enums.ConsoleTypeConverter
import io.vonley.mi.models.enums.FeaturesConverter
import io.vonley.mi.models.enums.ProtocolTypeConverter

@Database(
    entities = [
        Console::class
    ], version = 3, exportSchema = true
)
@TypeConverters(ConsoleTypeConverter::class, FeaturesConverter::class, ProtocolTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
}