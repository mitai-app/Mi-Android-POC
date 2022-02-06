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
import io.vonley.mi.di.network.protocols.ccapi.CCAPI
import io.vonley.mi.di.network.protocols.ccapi.CCAPIImpl
import io.vonley.mi.di.network.protocols.goldenhen.Goldhen
import io.vonley.mi.di.network.protocols.goldenhen.GoldhenImpl
import io.vonley.mi.di.network.protocols.klog.KLog
import io.vonley.mi.di.network.protocols.klog.KLogImpl
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIImpl
import io.vonley.mi.di.network.protocols.webman.WebManImpl
import io.vonley.mi.di.network.protocols.webman.Webman
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    @Singleton
    abstract fun bindMiFtpClientService(impl: MiFTPClientImpl): MiFTPClient

    @Binds
    @Singleton
    abstract fun bindCCAPIService(impl: CCAPIImpl): CCAPI

    @Binds
    @Singleton
    abstract fun bindGoldhenService(impl: GoldhenImpl): Goldhen

    @Binds
    @Singleton
    abstract fun bindWebManService(impl: WebManImpl): Webman

    @Binds
    @Singleton
    abstract fun bindPS3MAPIService(impl: PS3MAPIImpl): PS3MAPI

    @Binds
    @Singleton
    abstract fun bindKLogService(impl: KLogImpl): KLog

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