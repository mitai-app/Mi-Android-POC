package nyc.vonley.mi.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import nyc.vonley.mi.ui.main.console.ConsoleContract
import nyc.vonley.mi.ui.main.console.ConsoleFragment
import nyc.vonley.mi.ui.main.console.ConsolePresenter
import nyc.vonley.mi.ui.main.ftp.FTPContract
import nyc.vonley.mi.ui.main.ftp.FTPFragment
import nyc.vonley.mi.ui.main.ftp.FTPPresenter
import nyc.vonley.mi.ui.main.home.HomeContract
import nyc.vonley.mi.ui.main.home.HomeFragment
import nyc.vonley.mi.ui.main.home.HomePresenter
import nyc.vonley.mi.ui.main.payload.PayloadContract
import nyc.vonley.mi.ui.main.payload.PayloadFragment
import nyc.vonley.mi.ui.main.payload.PayloadPresenter
import nyc.vonley.mi.ui.main.settings.SettingsContract
import nyc.vonley.mi.ui.main.settings.SettingsFragment
import nyc.vonley.mi.ui.main.settings.SettingsPresenter

@Module
@InstallIn(FragmentComponent::class)
abstract class FragmentPresenterModule {

    @Binds
    @FragmentScoped
    abstract fun bindConsoleFragment(impl: ConsoleFragment): ConsoleContract.View

    @Binds
    @FragmentScoped
    abstract fun bindConsolePresenter(impl: ConsolePresenter): ConsoleContract.Presenter

    @Binds
    @FragmentScoped
    abstract fun bindSettingsFragment(impl: SettingsFragment): SettingsContract.View

    @Binds
    @FragmentScoped
    abstract fun bindSettingsPresenter(impl: SettingsPresenter): SettingsContract.Presenter

    @Binds
    @FragmentScoped
    abstract fun bindPayloadFragment(impl: PayloadFragment): PayloadContract.View

    @Binds
    @FragmentScoped
    abstract fun bindPayloadPresenter(impl: PayloadPresenter): PayloadContract.Presenter

    @Binds
    @FragmentScoped
    abstract fun bindHomeFragment(impl: HomeFragment): HomeContract.View

    @Binds
    @FragmentScoped
    abstract fun bindHomePresenter(impl: HomePresenter): HomeContract.Presenter

    @Binds
    @FragmentScoped
    abstract fun bindFTPPresenter(impl: FTPPresenter): FTPContract.Presenter

    @Binds
    @FragmentScoped
    abstract fun bindFTPFragment(impl: FTPFragment): FTPContract.View

    @Binds
    @FragmentScoped
    abstract fun bindViewModelStoreOwner(fragment: Fragment): ViewModelStoreOwner

}