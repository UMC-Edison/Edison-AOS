package com.umc.edison.data.token

import javax.inject.Inject
import javax.inject.Singleton
import com.umc.edison.data.datasources.PrefDataSource
import com.umc.edison.data.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class TokenManager @Inject constructor(
    private val prefDataSource: PrefDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : AccessTokenProvider {

    private val mutex = Mutex()

    init {
        applicationScope.launch {
            preloadTokens()
            loadUserId()
        }
    }

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null
    private var cachedUserId: String? = null


    override fun getAccessToken(): String? = cachedAccessToken

    override fun getRefreshToken(): String? = cachedRefreshToken

    suspend fun getUserId(): String? {
        if (cachedUserId != null) {
            return cachedUserId
        }
        return loadUserId()
    }

    override suspend fun clearCachedTokens() {
        mutex.withLock {
            cachedAccessToken = null
            cachedRefreshToken = null
        }
    }

    override suspend fun setCachedTokens(accessToken: String, refreshToken: String?) {
        mutex.withLock {
            cachedAccessToken = accessToken
            cachedRefreshToken = refreshToken
        }
    }

    suspend fun loadAccessToken(): String? {
        return mutex.withLock {
            val token = prefDataSource.get(ACCESS_TOKEN_KEY, "")
            cachedAccessToken = token.ifEmpty { null }
            token
        }
    }

    suspend fun loadRefreshToken(): String? {
        return mutex.withLock {
            val token = prefDataSource.get(REFRESH_TOKEN_KEY, "")
            cachedRefreshToken = token.ifEmpty { null }
            token
        }
    }

    suspend fun loadUserId(): String? {
        val id = prefDataSource.get(USER_ID_KEY, "")
        cachedUserId = id.ifEmpty { null }
        return cachedUserId
    }

    suspend fun saveUserId(userId: String) {
        cachedUserId = userId
        prefDataSource.set(USER_ID_KEY, userId)
    }

    suspend fun setToken(accessToken: String, refreshToken: String? = null) {
        mutex.withLock {
            cachedAccessToken = accessToken
            prefDataSource.set(ACCESS_TOKEN_KEY, accessToken)
            refreshToken?.let {
                prefDataSource.set(REFRESH_TOKEN_KEY, it)
                cachedRefreshToken = it
            }
        }
    }

    suspend fun deleteToken() {
        mutex.withLock {
            cachedAccessToken = null
            cachedRefreshToken = null
            prefDataSource.remove(ACCESS_TOKEN_KEY)
            prefDataSource.remove(REFRESH_TOKEN_KEY)
            prefDataSource.remove(USER_ID_KEY)
        }
    }

    private suspend fun preloadTokens() {
        mutex.withLock {
            cachedAccessToken = prefDataSource.get(ACCESS_TOKEN_KEY, "").ifEmpty { null }
            cachedRefreshToken = prefDataSource.get(REFRESH_TOKEN_KEY, "").ifEmpty { null }
        }
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
    }
}
