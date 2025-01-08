package com.umc.edison.remote.di

import com.umc.edison.remote.api.BubbleSpaceApiService
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
}