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
    // TODO: 추후 Retrofit을 싱글톤으로 관리하도록 수정 필요
    fun provideBubbleSpaceService(retrofit: Retrofit): BubbleSpaceApiService {
        return retrofit.create(BubbleSpaceApiService::class.java)
    }
}