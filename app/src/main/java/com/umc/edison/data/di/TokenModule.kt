package com.umc.edison.data.di

import com.umc.edison.data.datasources.PrefDataSource
import com.umc.edison.data.datasources.UserRemoteDataSource
import com.umc.edison.data.token.AccessTokenProvider
import com.umc.edison.data.token.DefaultTokenRetryHandler
import com.umc.edison.data.token.TokenManager
import com.umc.edison.data.token.TokenRetryHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {

    @Provides
    @Singleton
    fun provideAccessTokenProvider(tokenManager: TokenManager): AccessTokenProvider = tokenManager

    @Provides
    @Singleton
    fun provideTokenManager(
        prefDataSource: PrefDataSource,
        @ApplicationScope applicationScope: CoroutineScope
    ): TokenManager = TokenManager(prefDataSource, applicationScope)

    @Provides
    @Singleton
    fun provideTokenRetryHandler(
        userRemoteDataSource: UserRemoteDataSource,
        tokenManager: TokenManager
    ): TokenRetryHandler = DefaultTokenRetryHandler(userRemoteDataSource, tokenManager)
}