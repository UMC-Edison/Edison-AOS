package com.umc.edison.data.di

import com.umc.edison.data.repository.ArtLetterRepositoryImpl
import com.umc.edison.data.repository.BubbleRepositoryImpl
import com.umc.edison.data.repository.IdentityRepositoryImpl
import com.umc.edison.data.repository.LabelRepositoryImpl
import com.umc.edison.data.repository.OnboardingRepositoryImpl
import com.umc.edison.data.repository.RecentSearchRepositoryImpl
import com.umc.edison.data.repository.SyncRepositoryImpl
import com.umc.edison.data.repository.UserRepositoryImpl
import com.umc.edison.domain.repository.ArtLetterRepository
import com.umc.edison.domain.repository.BubbleRepository
import com.umc.edison.domain.repository.IdentityRepository
import com.umc.edison.domain.repository.LabelRepository
import com.umc.edison.domain.repository.OnboardingRepository
import com.umc.edison.domain.repository.RecentSearchRepository
import com.umc.edison.domain.repository.SyncRepository
import com.umc.edison.domain.repository.UserRepository
import com.umc.edison.data.repository.S3RepositoryImpl
import com.umc.edison.domain.repository.S3Repository

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
    abstract fun bindBubbleRepository(
        bubbleRepositoryImpl: BubbleRepositoryImpl
    ): BubbleRepository

    @Binds
    @Singleton
    abstract fun bindLabelRepository(
        labelRepositoryImpl: LabelRepositoryImpl
    ): LabelRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindArtLetterRepository(
        artLetterRepositoryImpl: ArtLetterRepositoryImpl
    ) : ArtLetterRepository

    @Binds
    @Singleton
    abstract fun bindIdentityRepository(
        identityRepositoryImpl: IdentityRepositoryImpl
    ): IdentityRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(
        recentSearchRepositoryImpl: RecentSearchRepositoryImpl
    ): RecentSearchRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        onboardingRepositoryImpl: OnboardingRepositoryImpl
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindS3Repository(
        s3RepositoryImpl: S3RepositoryImpl
    ): S3Repository

}