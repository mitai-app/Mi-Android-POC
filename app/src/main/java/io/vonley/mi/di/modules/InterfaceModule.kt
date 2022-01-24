package io.vonley.mi.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.di.network.MiFTPClient
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.impl.MiFTPClientImpl
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.di.network.impl.SyncServiceImpl
import io.vonley.mi.di.network.impl.PSXServiceImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    abstract fun bindMiFtpClientService(impl: MiFTPClientImpl): MiFTPClient

    @Binds
    abstract fun bindMiServer(impl: MiServerImpl): MiServer

    @Binds
    abstract fun bindClientService(impl: SyncServiceImpl): SyncService

    @Binds
    abstract fun bindPS4Client(impl: PSXServiceImpl): PSXService

}