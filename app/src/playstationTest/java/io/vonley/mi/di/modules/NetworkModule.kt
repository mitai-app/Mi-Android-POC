package io.vonley.mi.di.modules

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.vonley.mi.di.annotations.*
import io.vonley.mi.di.network.MiServer
import io.vonley.mi.di.network.PSXService
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.di.network.auth.OAuth2Authenticator
import io.vonley.mi.di.network.impl.MiFTPClientImpl
import io.vonley.mi.di.network.impl.MiServerImpl
import io.vonley.mi.di.network.impl.PSXServiceImpl
import io.vonley.mi.di.network.impl.SyncServiceImpl
import io.vonley.mi.di.network.protocols.ccapi.CCAPIImpl
import io.vonley.mi.di.network.protocols.goldenhen.GoldhenImpl
import io.vonley.mi.di.network.protocols.klog.KLogImpl
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPIImpl
import io.vonley.mi.di.network.protocols.webman.WebManImplTest
import io.vonley.mi.persistence.AppDatabase
import io.vonley.mi.utils.SharedPreferenceManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val LOG = true

    const val BASE_URL = "http://192.168.1.45"

    @Provides
    @Singleton
    fun provideCCAPIService(
        service: PSXService,
    ): CCAPIImpl {
        return CCAPIImpl(service)
    }

    @Provides
    @Singleton
    fun provideKlogService(
        service: PSXService,
    ): KLogImpl {
        return KLogImpl(service)
    }

    @Provides
    @Singleton
    fun provideWebManService(
        service: PSXService,
        @ApplicationContext context: Context
    ): WebManImplTest {
        return WebManImplTest(service, context.assets)
    }

    @Provides
    @Singleton
    fun providePS3MAPIService(
        service: PSXService,
    ): PS3MAPIImpl {
        return PS3MAPIImpl(service)
    }

    @Provides
    @Singleton
    fun providePS3MAPIService(
        service: PSXService,
        server: MiServer
    ): GoldhenImpl {
        return GoldhenImpl(service, server)
    }


    @Provides
    @Singleton
    fun provideClientSyncService(
        @ApplicationContext context: Context,
        database: AppDatabase,
        @SharedPreferenceStorage manager: SharedPreferenceManager,
        @GuestInterceptorOkHttpClient client: OkHttpClient
    ): SyncServiceImpl {
        return SyncServiceImpl(context, database, manager, client)
    }

    @Provides
    @Singleton
    fun provideMiJbServer(
        @ApplicationContext context: Context,
        @SharedPreferenceStorage manager: SharedPreferenceManager,
        service: PSXService
    ): MiServerImpl {
        return MiServerImpl(context, manager, service)
    }


    @Provides
    @Singleton
    fun providePS4Service(
        @GuestInterceptorOkHttpClient http: OkHttpClient,
        sync: SyncService,
        @SharedPreferenceStorage manager: SharedPreferenceManager
    ): PSXServiceImpl {
        return PSXServiceImpl(sync, http, manager)
    }

    @Provides
    @Singleton
    fun provideFTPService(
        @SharedPreferenceStorage manager: SharedPreferenceManager,
        sync: SyncService
    ): MiFTPClientImpl {
        return MiFTPClientImpl(manager, sync)
    }

    @AuthInterceptorOkHttpClient
    @Provides
    fun provideAuthInterceptor(
        @SharedPreferenceStorage manager: SharedPreferenceManager
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .authenticator(OAuth2Authenticator(manager))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(Environment.getDownloadCacheDirectory(), (20 * 1024 * 1024).toLong()))
        if (LOG) {
            builder.addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.HEADERS
            }).addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BODY
            })
        }
        return builder.build()
    }

    @GuestInterceptorOkHttpClient
    @Provides
    fun provideGuestInterceptor(
        @SharedPreferenceStorage manager: SharedPreferenceManager
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(Environment.getDownloadCacheDirectory(), (20 * 1024 * 1024).toLong()))
        if (LOG) {
            builder.addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.HEADERS
            }).addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BODY
            })
        }
        return builder.build()
    }


    @Provides
    @AuthRetrofitClient
    fun provideAuthenticatedRetrofit(
        @AuthInterceptorOkHttpClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .client(okHttpClient)
            .build()
    }


    @Provides
    @GuestRetrofitClient
    fun provideGuestService(
        @GuestInterceptorOkHttpClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .client(okHttpClient)
            .build()
    }


}