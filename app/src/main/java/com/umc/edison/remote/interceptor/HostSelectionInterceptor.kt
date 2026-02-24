package com.umc.edison.remote.interceptor

import com.umc.edison.remote.config.DomainProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HostSelectionInterceptor @Inject constructor(
    private val domainProvider: DomainProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val base = domainProvider.getDomain().toHttpUrlOrNull()
        val newReq = base?.let {
            val newUrl = originalRequest.url.newBuilder()
                .scheme(it.scheme)
                .host(it.host)
                .port(it.port)
                .build()

            originalRequest.newBuilder()
                .url(newUrl)
                .build()
        } ?: originalRequest
        return chain.proceed(newReq)
    }
}
