package nyc.vonley.mi.di.modules

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.FragmentScoped
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.di.repository.ConsoleRepository
import nyc.vonley.mi.persistence.ConsoleDao
import nyc.vonley.mi.ui.main.console.ConsoleFragment
import nyc.vonley.mi.ui.main.console.ConsoleViewModel

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideConsoleRepository(
        service: ClientSync,
        consoleDao: ConsoleDao
    ): ConsoleRepository {
        return ConsoleRepository(service, consoleDao)
    }

}