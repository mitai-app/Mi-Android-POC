package io.vonley.mi.di.modules

import android.app.Application

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.helpers.Voice
import io.vonley.mi.helpers.impl.VoiceImpl
import io.vonley.mi.persistence.AppDatabase
import io.vonley.mi.persistence.ConsoleDao
import io.vonley.mi.utils.SharedPreferenceManager
import io.vonley.mi.utils.SharedPreferenceManagerImpl
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
    fun provideTTS(
        context: Application,
        @SharedPreferenceStorage manager: SharedPreferenceManager
    ): Voice {
        return VoiceImpl(context, manager)
    }

    @Provides
    @Singleton
    fun provideConsoleDao(appDatabase: AppDatabase): ConsoleDao {
        return appDatabase.consoleDao()
    }

    /**
     * Consider looking into Datastore
     */
    @Provides
    @Singleton
    @SharedPreferenceStorage
    fun provideSharePreferenceManager(
        @ApplicationContext context: Context
    ): SharedPreferenceManager {
        val preferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        return SharedPreferenceManagerImpl(context, preferences)
    }

}