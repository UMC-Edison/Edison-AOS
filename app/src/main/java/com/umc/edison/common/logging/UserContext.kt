package com.umc.edison.common.logging

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.umc.edison.data.datasources.PrefDataSource
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserContext @Inject constructor(
    private val prefDataSource: PrefDataSource
) {
    companion object {
        private const val KEY_INSTALL_ID = "install_id"
    }

    suspend fun ensureInstallId(): String {
        val existing: String = prefDataSource.get(KEY_INSTALL_ID, "")
        if (existing.isNotBlank()) {
            Firebase.crashlytics.setCustomKey(KEY_INSTALL_ID, existing)
            return existing
        }
        val newId = UUID.randomUUID().toString()
        prefDataSource.set(KEY_INSTALL_ID, newId)
        Firebase.crashlytics.setCustomKey(KEY_INSTALL_ID, newId)
        return newId
    }

    fun setAccountId(accountId: String) {
        Firebase.crashlytics.setUserId(accountId)
    }

    fun setBuildInfo(buildType: String, applicationId: String, versionName: String) {
        Firebase.crashlytics.setCustomKey("build_type", buildType)
        Firebase.crashlytics.setCustomKey("application_id", applicationId)
        Firebase.crashlytics.setCustomKey("version_name", versionName)
    }
}
