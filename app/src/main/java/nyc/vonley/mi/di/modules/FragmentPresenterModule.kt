package nyc.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.ui.main.MainActivity
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.ui.main.MainPresenter
import nyc.vonley.mi.ui.main.console.ConsoleContract
import nyc.vonley.mi.ui.main.console.ConsoleFragment
import nyc.vonley.mi.ui.main.console.ConsolePresenter

@Module
@InstallIn(FragmentComponent::class)
abstract class FragmentPresenterModule {

    @Binds
    @FragmentScoped
    abstract fun bindConsoleFragment(impl: ConsoleFragment): ConsoleContract.View

    @Binds
    @FragmentScoped
    abstract fun bindConsolePresenter(impl: ConsolePresenter): ConsoleContract.Presenter

}