package nyc.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nyc.vonley.mi.di.network.MiFTPClient
import nyc.vonley.mi.di.network.SyncService
import nyc.vonley.mi.di.network.PSXService
import nyc.vonley.mi.di.network.impl.MiFTPClientImpl
import nyc.vonley.mi.di.network.impl.SyncServiceImpl
import nyc.vonley.mi.di.network.impl.PSXServiceImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    abstract fun bindMiFtpClientService(impl: MiFTPClientImpl): MiFTPClient

    @Binds
    abstract fun bindClientService(impl: SyncServiceImpl): SyncService

    @Binds
    abstract fun bindPS4Client(impl: PSXServiceImpl): PSXService

}