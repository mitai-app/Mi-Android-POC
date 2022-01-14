package nyc.vonley.mi.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.di.repository.ConsoleRepository
import nyc.vonley.mi.persistence.ConsoleDao

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