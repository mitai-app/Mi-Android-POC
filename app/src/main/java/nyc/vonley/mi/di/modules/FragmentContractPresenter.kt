package nyc.vonley.mi.di.modules

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import nyc.vonley.mi.di.repository.ConsoleRepository
import nyc.vonley.mi.ui.main.console.ConsoleFragment
import nyc.vonley.mi.ui.main.console.ConsoleViewModel
import nyc.vonley.mi.ui.main.payload.PayloadFragment

@Module
@InstallIn(FragmentComponent::class)
object FragmentContractPresenter {

    @Provides
    fun provideConsoleFragment(activity: Fragment): ConsoleFragment {
        return activity as ConsoleFragment
    }

    @Provides
    fun providePayloadFragment(activity: Fragment): PayloadFragment {
        return activity as PayloadFragment
    }

    @Provides
    @FragmentScoped
    fun provideConsoleViewModelFactory(
        application: Application,
        repository: ConsoleRepository
    ): ConsoleViewModel.Factory {
        return ConsoleViewModel.Factory(application, repository)
    }

    @Provides
    @FragmentScoped
    fun provideConsoleViewModel(
        fragment: ViewModelStoreOwner,
        factory: ConsoleViewModel.Factory
    ): ConsoleViewModel {
        return ViewModelProvider(fragment, factory)[ConsoleViewModel::class.java]
    }

}