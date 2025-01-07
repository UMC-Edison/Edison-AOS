package com.umc.edison.remote.token

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AccessTokenInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.loadAccessToken()
        val requestBuilder = chain.request().newBuilder()

        if (token != null) {
            requestBuilder.addHeader(HEADER_AUTHORIZATION, "$TOKEN_TYPE $token")
        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
