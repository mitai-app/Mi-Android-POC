package nyc.vonley.mi.di.modules

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import nyc.vonley.mi.ui.main.console.ConsoleFragment
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

}