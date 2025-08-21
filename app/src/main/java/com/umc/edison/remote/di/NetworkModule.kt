package com.umc.edison.remote.di

import com.umc.edison.BuildConfig
import com.umc.edison.remote.token.AccessTokenInterceptor
import com.umc.edison.data.token.TokenManager
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

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RefreshRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class S3Retrofit

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class S3Client

    @Provides
    @Singleton
    fun provideConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAccessTokenInterceptor(tokenManager: TokenManager) = AccessTokenInterceptor(tokenManager)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        accessTokenInterceptor: AccessTokenInterceptor,
    ) : OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(accessTokenInterceptor)
        .build()

    @Provides @Singleton @S3Client
    fun provideS3OkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .build()

    @MainRetrofit
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ) : Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(gsonConverterFactory)
        .client(okHttpClient)
        .build()

    @S3Retrofit
    @Provides
    @Singleton
    fun provideS3Retrofit(
        gsonConverterFactory: GsonConverterFactory,
        @S3Client okHttpClient: OkHttpClient,
    ) : Retrofit = Retrofit.Builder()
        .baseUrl("https://s3.amazonaws.com/")
        .addConverterFactory(gsonConverterFactory)
        .client(okHttpClient)
        .build()

    @RefreshRetrofit
    @Provides
    @Singleton
    fun provideRefreshRetrofit(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .client(okHttpClient)
            .build()
    }
}