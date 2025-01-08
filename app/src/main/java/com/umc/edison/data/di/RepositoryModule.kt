package com.umc.edison.data.di

import com.umc.edison.data.repository.BubbleSpaceRepositoryImpl
import com.umc.edison.domain.repository.BubbleSpaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBubbleSpaceRepository(
        bubbleSpaceRepositoryImpl: BubbleSpaceRepositoryImpl
    ): BubbleSpaceRepository
}