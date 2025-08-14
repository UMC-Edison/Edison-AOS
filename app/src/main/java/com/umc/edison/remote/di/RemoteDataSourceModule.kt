package com.umc.edison.remote.di

import com.umc.edison.data.datasources.ArtLetterRemoteDataSource
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.datasources.LabelRemoteDataSource
import com.umc.edison.data.datasources.S3RemoteDataSource
import com.umc.edison.data.datasources.UserRemoteDataSource
import com.umc.edison.remote.datasources.ArtLetterRemoteDataSourceImpl
import com.umc.edison.remote.datasources.BubbleRemoteDataSourceImpl
import com.umc.edison.remote.datasources.LabelRemoteDataSourceImpl
import com.umc.edison.remote.datasources.S3RemoteDataSourceImpl
import com.umc.edison.remote.datasources.UserRemoteDataSourceImpl
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
    abstract fun bindBubbleRemoteDataSource(
        bubbleRemoteDataSourceImpl: BubbleRemoteDataSourceImpl
    ): BubbleRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindLabelRemoteDataSource(
        labelRemoteDataSourceImpl: LabelRemoteDataSourceImpl
    ): LabelRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(
        userRemoteDataSourceImpl: UserRemoteDataSourceImpl
    ): UserRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindArtLetterRemoteDataSource(
        artLetterRemoteDataSourceImpl: ArtLetterRemoteDataSourceImpl
    ): ArtLetterRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindS3RemoteDataSource(
        impl: S3RemoteDataSourceImpl
    ): S3RemoteDataSource
}
