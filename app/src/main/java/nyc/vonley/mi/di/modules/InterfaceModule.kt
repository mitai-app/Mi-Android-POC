package nyc.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.di.network.impl.ClientSyncService


@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {


    @Binds
    abstract fun bindClientSync(impl: ClientSyncService): ClientSync

}