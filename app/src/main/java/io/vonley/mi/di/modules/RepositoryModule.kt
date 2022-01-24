package io.vonley.mi.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.repository.ConsoleRepository
import io.vonley.mi.persistence.ConsoleDao

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideConsoleRepository(
        service: SyncService,
        consoleDao: ConsoleDao
    ): ConsoleRepository {
        return ConsoleRepository(service, consoleDao)
    }

}