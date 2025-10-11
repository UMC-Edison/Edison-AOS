package com.umc.edison.data.token

import javax.inject.Inject
import javax.inject.Singleton
import com.umc.edison.data.datasources.PrefDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class TokenManager @Inject constructor(
    private val prefDataSource: PrefDataSource
) : AccessTokenProvider {

    init {
        CoroutineScope(Dispatchers.IO).apply {
            launch {
                loadAccessToken()
                loadRefreshToken()
            }
        }
    }

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    override fun getAccessToken(): String? {
        if (cachedAccessToken == null) {
            println("⚠️ Warning: access token not cached. Consider calling loadAccessToken() at app startup.")
        }
        return cachedAccessToken
    }

    override fun getRefreshToken(): String? {
        if (cachedRefreshToken == null) {
            println("⚠️ Warning: refresh token not cached. Consider calling loadRefreshToken() at app startup.")
        }
        return cachedRefreshToken
    }

    override fun clearCachedTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
    }

    override fun setCachedTokens(accessToken: String, refreshToken: String?) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
    }

    suspend fun loadAccessToken(): String? {
        val token = prefDataSource.get(ACCESS_TOKEN_KEY, "")
        cachedAccessToken = token.ifEmpty { null }

        return token
    }

    suspend fun loadRefreshToken(): String? {
        val token = prefDataSource.get(REFRESH_TOKEN_KEY, "")
        cachedRefreshToken = token.ifEmpty { null }

        return token
    }

    suspend fun setToken(accessToken: String, refreshToken: String? = null) {
        cachedAccessToken = accessToken
        prefDataSource.set(ACCESS_TOKEN_KEY, accessToken)
        refreshToken?.let {
            prefDataSource.set(REFRESH_TOKEN_KEY, it)
            cachedRefreshToken = it
        }
    }

    suspend fun deleteToken() {
        cachedAccessToken = null
        cachedRefreshToken = null
        prefDataSource.remove(ACCESS_TOKEN_KEY)
        prefDataSource.remove(REFRESH_TOKEN_KEY)
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
