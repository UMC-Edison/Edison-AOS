package com.umc.edison.data.token

import com.umc.edison.common.logging.AppLogger
import com.umc.edison.data.datasources.PrefDataSource
import com.umc.edison.data.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class TokenManager @Inject constructor(
    private val prefDataSource: PrefDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : AccessTokenProvider {

    private val mutex = Mutex()
    private val loadJob = applicationScope.launch { preloadTokens() }

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    override fun getAccessToken(): String? {
        ensureLoaded()
        return cachedAccessToken
    }

    override fun getRefreshToken(): String? {
        ensureLoaded()
        return cachedRefreshToken
    }

    override fun clearCachedTokens() {
        runBlocking {
            mutex.withLock {
                cachedAccessToken = null
                cachedRefreshToken = null
            }
        }
    }

    override fun setCachedTokens(accessToken: String, refreshToken: String?) {
        runBlocking {
            mutex.withLock {
                cachedAccessToken = accessToken
                cachedRefreshToken = refreshToken
            }
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
        }
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"

        private const val TAG = "TokenManager"
    }

    private suspend fun preloadTokens() {
        mutex.withLock {
            cachedAccessToken = prefDataSource.get(ACCESS_TOKEN_KEY, "").ifEmpty { null }
            cachedRefreshToken = prefDataSource.get(REFRESH_TOKEN_KEY, "").ifEmpty { null }
        }
    }

    private fun ensureLoaded() {
        if (cachedAccessToken != null || cachedRefreshToken != null) return
        if (!loadJob.isCompleted) {
            runBlocking {
                try {
                    loadJob.join()
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to preload tokens: ${e.message}", e)
                }
            }
        }
    }
}
