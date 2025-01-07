package com.umc.edison.remote.di

import com.umc.edison.data.datasources.BubbleSpaceRemoteDataSource
import com.umc.edison.remote.datasources.BubbleSpaceRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindBubbleSpaceRemoteDataSource(
        bubbleSpaceRemoteDataSourceImpl: BubbleSpaceRemoteDataSourceImpl
    ): BubbleSpaceRemoteDataSource
}