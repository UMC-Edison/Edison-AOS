package com.umc.edison.remote.di

import com.umc.edison.remote.api.ArtLetterApiService
import com.umc.edison.remote.api.BubbleApiService
import com.umc.edison.remote.api.BubbleSpaceApiService
import com.umc.edison.remote.api.LabelApiService
import com.umc.edison.remote.api.LoginApiService
import com.umc.edison.remote.api.S3ApiService
import com.umc.edison.remote.api.MyPageApiService
import com.umc.edison.remote.api.RefreshTokenApiService
import com.umc.edison.remote.api.SyncApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ServiceModule {
    @Provides
    @Singleton
    fun provideBubbleSpaceService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): BubbleSpaceApiService = retrofit.create(BubbleSpaceApiService::class.java)

    @Provides
    @Singleton
    fun provideSyncService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): SyncApiService = retrofit.create(SyncApiService::class.java)

    @Provides
    @Singleton
    fun provideMyPageService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): MyPageApiService = retrofit.create(MyPageApiService::class.java)

    @Provides
    @Singleton
    fun provideRefreshTokenService(
        @NetworkModule.RefreshRetrofit retrofit: Retrofit
    ): RefreshTokenApiService = retrofit.create(RefreshTokenApiService::class.java)

    @Provides
    @Singleton
    fun provideLoginApiService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): LoginApiService = retrofit.create(LoginApiService::class.java)

    @Provides
    @Singleton
    fun provideArtLetterApiService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): ArtLetterApiService = retrofit.create(ArtLetterApiService::class.java)

    @Provides
    @Singleton
    fun provideMyEdisonApiService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): S3ApiService = retrofit.create(S3ApiService::class.java)

    @Provides
    @Singleton
    fun provideBubbleApiService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): BubbleApiService = retrofit.create(BubbleApiService::class.java)

    @Provides
    @Singleton
    fun provideLabelApiService(
        @NetworkModule.MainRetrofit retrofit: Retrofit
    ): LabelApiService = retrofit.create(LabelApiService::class.java)
}