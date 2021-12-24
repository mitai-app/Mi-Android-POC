package nyc.vonley.mi.di.modules

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.persistence.AppDatabase
import nyc.vonley.mi.utils.SharedPreferenceManager
import nyc.vonley.mi.utils.SharedPreferenceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {

    const val PREFERENCE_FILE: String = "mi.prefs"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "mi.db")
            .fallbackToDestructiveMigration()
            .build()

    }


    @Provides
    @Singleton
    @SharedPreferenceStorage
    fun provideSharePreferenceManager(
        @ApplicationContext context: Context
    ): SharedPreferenceManager {
        val preferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return SharedPreferenceManagerImpl(preferences)
    }

}