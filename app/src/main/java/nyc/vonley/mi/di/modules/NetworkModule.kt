package nyc.vonley.mi.di.modules

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nyc.vonley.mi.di.annotations.AuthInterceptorOkHttpClient
import nyc.vonley.mi.di.annotations.AuthRetrofitClient
import nyc.vonley.mi.di.annotations.GuestRetrofitClient
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.auth.OAuth2Authenticator
import nyc.vonley.mi.di.network.impl.ClientSyncService
import nyc.vonley.mi.persistence.AppDatabase
import nyc.vonley.mi.utils.SharedPreferenceManager
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
    fun provideClientSyncService(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): ClientSyncService {
        return ClientSyncService(context, database)
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