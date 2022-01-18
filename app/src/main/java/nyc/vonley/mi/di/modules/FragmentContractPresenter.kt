package nyc.vonley.mi.di.modules

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.preference.PreferenceFragmentCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import nyc.vonley.mi.di.repository.ConsoleRepository
import nyc.vonley.mi.ui.main.console.ConsoleFragment
import nyc.vonley.mi.ui.main.console.ConsoleViewModel
import nyc.vonley.mi.ui.main.ftp.FTPFragment
import nyc.vonley.mi.ui.main.home.HomeFragment
import nyc.vonley.mi.ui.main.payload.PayloadFragment
import nyc.vonley.mi.ui.main.settings.SettingsFragment

@Module
@InstallIn(FragmentComponent::class)
object FragmentContractPresenter {

    @Provides
    fun provideSettingFragment(activity: PreferenceFragmentCompat): SettingsFragment {
        return activity as SettingsFragment
    }

    @Provides
    fun provideConsoleFragment(activity: Fragment): ConsoleFragment {
        return activity as ConsoleFragment
    }

    @Provides
    fun providePayloadFragment(activity: Fragment): PayloadFragment {
        return activity as PayloadFragment
    }

    @Provides
    fun provideHomeFragment(activity: Fragment): HomeFragment {
        return activity as HomeFragment
    }

    @Provides
    fun provideFTPFragment(activity: Fragment): FTPFragment {
        return activity as FTPFragment
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