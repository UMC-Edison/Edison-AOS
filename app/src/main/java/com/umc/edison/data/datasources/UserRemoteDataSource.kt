package com.umc.edison.data.datasources

import com.umc.edison.data.model.identity.IdentityCategoryEntity
import com.umc.edison.data.model.identity.IdentityEntity
import com.umc.edison.data.model.user.UserEntity
import com.umc.edison.data.model.user.UserWithTokenEntity

interface UserRemoteDataSource {
    // CREATE
    suspend fun addIdentity(identity: IdentityEntity)
    suspend fun googleLogin(idToken: String): UserWithTokenEntity
    suspend fun googleSignup(
        idToken: String,
        nickname: String,
        identity: List<IdentityEntity>
    ): UserWithTokenEntity
    suspend fun refreshAccessToken(refreshToken: String): String

    // READ
    suspend fun getAllMyIdentityResults(): List<IdentityEntity>
    suspend fun getAllRecentSearches(): List<String>
    suspend fun getIdentityByCategory(category: IdentityCategoryEntity): IdentityEntity
    suspend fun getMyProfileInfo(): UserEntity

    // UPDATE
    suspend fun logOut()
    suspend fun updateIdentity(identity: IdentityEntity)
    suspend fun updateProfileInfo(user: UserEntity)

    // DELETE
    suspend fun deleteRecentSearch(search: String)
    suspend fun deleteUser()
}