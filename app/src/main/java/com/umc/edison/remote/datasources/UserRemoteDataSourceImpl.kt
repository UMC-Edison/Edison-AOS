package com.umc.edison.remote.datasources

import android.util.Log
import com.umc.edison.data.datasources.UserRemoteDataSource
import com.umc.edison.data.model.identity.IdentityCategoryEntity
import com.umc.edison.data.model.identity.IdentityEntity
import com.umc.edison.data.model.user.UserEntity
import com.umc.edison.data.model.user.UserWithTokenEntity
import com.umc.edison.remote.api.ArtLetterApiService
import com.umc.edison.remote.api.LoginApiService
import com.umc.edison.remote.api.MyPageApiService
import com.umc.edison.remote.model.login.IdTokenRequest
import com.umc.edison.remote.model.login.toSetIdentityKeywordRequest
import com.umc.edison.remote.model.mypage.toUpdateTestRequest
import com.umc.edison.remote.model.mypage.toUpdateProfileRequest
import com.umc.edison.remote.api.RefreshTokenApiService
import com.umc.edison.remote.model.login.SignUpRequest
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
    private val loginApiService: LoginApiService,
    private val myPageApiService: MyPageApiService,
    private val artLetterApiService: ArtLetterApiService,
    private val refreshTokenApiService: RefreshTokenApiService,
) : UserRemoteDataSource {
    // CREATE
    override suspend fun addIdentity(identity: IdentityEntity) {
        loginApiService.setUserIdentityAndInterest(identity.toSetIdentityKeywordRequest())
    }

    override suspend fun googleLogin(idToken: String): UserWithTokenEntity {
        val request = IdTokenRequest(idToken)
        val response = loginApiService.googleLogin(request)

        if (!response.isSuccess) {
            throw Exception(response.code)
        }

        return response.data.toData()
    }

    override suspend fun googleSignup(
        idToken: String,
        nickname: String,
        identity: List<IdentityEntity>
    ): UserWithTokenEntity {
        val request = SignUpRequest(
            idToken = idToken,
            nickname = nickname,
            identity = identity.map { it.toSetIdentityKeywordRequest() }
        )

        val response = loginApiService.googleSignup(request)

        if (!response.isSuccess) {
            throw Exception(response.code)
        }
        return response.data.toData()
    }

    override suspend fun refreshAccessToken(refreshToken: String): String {
        return refreshTokenApiService.refreshToken(refreshToken).data.accessToken
    }

    // READ
    override suspend fun getAllMyIdentityResults(): List<IdentityEntity> {
        return myPageApiService.getAllMyTestResults().data.categories.toData()
    }

    override suspend fun getAllRecentSearches(): List<String> {
        return artLetterApiService.getRecentSearches().data.keywords
    }

    override suspend fun getIdentityByCategory(category: IdentityCategoryEntity): IdentityEntity {
        val keywords = myPageApiService.getTestKeyword(category.categoryNumber).data
        return IdentityEntity(
            category = category,
            keywords = keywords.map { it.toData() },
            selectedKeywords = emptyList()
        )
    }

    override suspend fun getMyProfileInfo(): UserEntity {
        return myPageApiService.getProfileInfo().data.toData()
    }

    // UPDATE
    override suspend fun logOut() {
        myPageApiService.logout()
    }

    override suspend fun updateIdentity(identity: IdentityEntity) {
        myPageApiService.updateTest(identity.toUpdateTestRequest())
    }

    override suspend fun updateProfileInfo(user: UserEntity) {
        myPageApiService.updateProfile(user.toUpdateProfileRequest())
    }

    // DELETE
    override suspend fun deleteRecentSearch(search: String) {
        artLetterApiService.removeRecentSearch(search)
    }

    override suspend fun deleteUser() {
        myPageApiService.deleteAccount()
    }
}