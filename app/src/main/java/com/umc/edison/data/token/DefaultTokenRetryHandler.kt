package com.umc.edison.data.token

import com.google.android.gms.common.api.ApiException
import com.umc.edison.data.datasources.UserRemoteDataSource
import javax.inject.Inject
import retrofit2.HttpException

class DefaultTokenRetryHandler @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val tokenManager: TokenManager
) : TokenRetryHandler {

    override suspend fun <T> runWithTokenRetry(dataAction: suspend () -> T): T {
        return try {
            dataAction()
        } catch (e: Throwable) {
            if (!isUnauthorized(e)) throw e

            val refreshToken = tokenManager.loadRefreshToken() ?: throw NoRefreshTokenException()

            val newAccessToken = userRemoteDataSource.refreshAccessToken(refreshToken)
            tokenManager.setToken(newAccessToken, refreshToken)

            dataAction()
        }
    }

    private fun isUnauthorized(e: Throwable): Boolean {
        return (e is ApiException && e.message == "LOGIN4004") ||
                (e is HttpException && e.code() == 401)
    }
}
