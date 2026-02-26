package com.umc.edison.remote.config

import com.umc.edison.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.Volatile

@Singleton
class DomainProvider @Inject constructor() {
    @Volatile private var current: String = BuildConfig.BASE_URL

    fun getDomain(): String = current
    fun setDomain(domain: String) {
        if (domain.isNotBlank() && domain != current) {
            current = domain
        }
    }
}
