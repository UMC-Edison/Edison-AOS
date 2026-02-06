package com.umc.edison.data.token

import javax.inject.Inject
import javax.inject.Singleton
import com.umc.edison.data.datasources.PrefDataSource
import com.umc.edison.data.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class TokenManager @Inject constructor(
    private val prefDataSource: PrefDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : AccessTokenProvider {

    init {
        applicationScope.launch {
            loadAccessToken()
            loadRefreshToken()
            loadUserId()
        }
    }

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null
    private var cachedUserId: String? = null

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

    fun getUserId(): String? = cachedUserId

    override fun clearCachedTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedUserId = null
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
        cachedUserId = null

        prefDataSource.remove(ACCESS_TOKEN_KEY)
        prefDataSource.remove(REFRESH_TOKEN_KEY)
        prefDataSource.remove(USER_ID_KEY)
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
    }
}
