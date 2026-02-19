package com.umc.edison.data.token

interface AccessTokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    suspend fun clearCachedTokens()
    suspend fun setCachedTokens(accessToken: String, refreshToken: String?)
}
