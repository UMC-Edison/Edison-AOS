package com.umc.edison.local.di

import com.umc.edison.data.datasources.BubbleSpaceLocalDataSource
import com.umc.edison.local.datasources.BubbleSpaceLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindBubbleSpaceLocalDataSource(
        bubbleSpaceLocalDataSourceImpl: BubbleSpaceLocalDataSourceImpl
    ): BubbleSpaceLocalDataSource
}