package com.umc.edison

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.umc.edison.common.logging.AppLogger
import com.umc.edison.data.di.EntryPointModule
import com.umc.edison.data.sync.SyncDataWorkerFactory
import com.umc.edison.presentation.sync.SyncTrigger
import com.umc.edison.remote.config.DomainProvider
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import io.branch.referral.Branch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.umc.edison.common.logging.UserContext
import com.umc.edison.remote.config.RemoteConfigKeys

@HiltAndroidApp
class EdisonApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var domainProvider: DomainProvider

    @Inject
    lateinit var userContext: UserContext

    private val remoteConfigScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val remoteConfig by lazy { Firebase.remoteConfig }
    private val maxAttempts = 4
    private val initialBackoffMs = 1_000L

    override fun onCreate() {
        super.onCreate()
        initCrashlyticsContext()
        initRemoteConfig()

        // Branch SDK 초기화
        Branch.getAutoInstance(this)

        // 기존 네트워크 상태 모니터링 초기화
        val syncTrigger = SyncTrigger(this)
        syncTrigger.setupSync()
    }

    private fun initCrashlyticsContext() {
        remoteConfigScope.launch {
            userContext.ensureInstallId()
            userContext.setBuildInfo(
                BuildConfig.BUILD_TYPE,
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME
            )
        }
    }

    private fun initRemoteConfig() {

        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 60 * 60 * 12)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.setDefaultsAsync(
            mapOf(RemoteConfigKeys.BASE_URL to BuildConfig.BASE_URL)
        )

        remoteConfig.getString(RemoteConfigKeys.BASE_URL)
            .takeIf { it.isNotBlank() }
            ?.let(domainProvider::setDomain)

        remoteConfigScope.launch {
            var backoff = initialBackoffMs
            repeat(maxAttempts) { attempt ->
                val activated = try {
                    remoteConfig.fetchAndActivate().await()
                } catch (e: Exception) {
                    AppLogger.w(
                        "EdisonApplication",
                        "Remote config fetch failed on attempt ${attempt + 1}",
                        e
                    )
                    false
                }

                if (activated) {
                    remoteConfig.getString(RemoteConfigKeys.BASE_URL)
                        .takeIf { it.isNotBlank() }
                        ?.let(domainProvider::setDomain)
                    return@launch
                }

                if (attempt < maxAttempts - 1) {
                    delay(backoff)
                    backoff = (backoff * 2).coerceAtMost(8_000L)
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() {
            val syncDataWorkerFactory: SyncDataWorkerFactory = EntryPoints.get(
                applicationContext,
                EntryPointModule::class.java
            ).getSyncDataWorkerFactory()

            return Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setWorkerFactory(syncDataWorkerFactory)
                .build()
        }
}
