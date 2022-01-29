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
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    @Singleton
    abstract fun bindMiFtpClientService(impl: MiFTPClientImpl): MiFTPClient

    @Binds
    @Singleton
    abstract fun bindMiServer(impl: MiServerImpl): MiServer

    @Binds
    @Singleton
    abstract fun bindClientService(impl: SyncServiceImpl): SyncService

    @Binds
    @Singleton
    abstract fun bindPS4Client(impl: PSXServiceImpl): PSXService

}