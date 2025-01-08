package com.umc.edison.remote.di

import com.umc.edison.BuildConfig
import com.umc.edison.remote.token.AccessTokenInterceptor
import com.umc.edison.remote.token.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TIME_OUT = 30

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MainRetrofit

    @Provides
    @Singleton
    fun provideConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideAccessTokenInterceptor(tokenManager: TokenManager) = AccessTokenInterceptor(tokenManager)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        accessTokenInterceptor: AccessTokenInterceptor
    ) : OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(accessTokenInterceptor)
        .build()

    @MainRetrofit
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ) : Retrofit = Retrofit.Builder()
        .addConverterFactory(gsonConverterFactory)
        .client(okHttpClient)
        .baseUrl(BuildConfig.BASE_URL)
        .build()
}