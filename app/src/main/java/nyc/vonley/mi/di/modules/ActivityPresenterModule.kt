package nyc.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import nyc.vonley.mi.ui.main.MainActivity
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.ui.main.MainPresenter

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityPresenterModule {

    @Binds
    @ActivityScoped
    abstract fun bindMainActivity(impl: MainActivity): MainContract.View

    @Binds
    @ActivityScoped
    abstract fun bindMainPresenter(impl: MainPresenter): MainContract.Presenter

}