package com.umc.edison.remote.token

import com.umc.edison.common.logging.AppLogger
import com.umc.edison.data.token.AccessTokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AccessTokenInterceptor @Inject constructor(
    private val tokenProvider: AccessTokenProvider
) : Interceptor {
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getAccessToken()

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader(HEADER_AUTHORIZATION, "$TOKEN_TYPE $token")
        } else {
            AppLogger.w("AccessTokenInterceptor", "Access token missing, sending request without Authorization header")
        }

        return chain.proceed(requestBuilder.build())
    }
}
