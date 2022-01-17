package nyc.vonley.mi.di.modules

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import nyc.vonley.mi.ui.main.MainActivity

@Module
@InstallIn(ActivityComponent::class)
object ActivityContractPresenter {

    @Provides
    fun provideMainActivity(activity: Activity): MainActivity {
        return activity as MainActivity
    }

}