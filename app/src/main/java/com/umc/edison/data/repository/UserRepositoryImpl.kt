package com.umc.edison.data.repository

import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.UserRemoteDataSource
import com.umc.edison.data.model.identity.toData
import com.umc.edison.data.model.user.UserWithTokenEntity
import com.umc.edison.data.model.user.toData
import com.umc.edison.data.token.TokenManager
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.identity.Identity
import com.umc.edison.domain.model.user.User
import com.umc.edison.domain.repository.BubbleRepository
import com.umc.edison.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val resourceFactory: FlowBoundResourceFactory,
    private val tokenManager: TokenManager,
) : UserRepository {

    override fun googleLogin(idToken: String): Flow<DataResource<User>> = resourceFactory.remote(
        dataAction = {
            val userWithToken: UserWithTokenEntity = userRemoteDataSource.googleLogin(idToken)
            tokenManager.setToken(userWithToken.accessToken, userWithToken.refreshToken)
            tokenManager.saveUserEmail(userWithToken.user.email)
            userWithToken
        }
    )

    override fun googleSignUp(
        idToken: String,
        nickname: String,
        identity: List<Identity>
    ): Flow<DataResource<User>> = resourceFactory.remote(
        dataAction = {
            val userWithToken: UserWithTokenEntity = userRemoteDataSource.googleSignup(
                idToken = idToken,
                nickname = nickname,
                identity = identity.map { it.toData() }
            )
            tokenManager.setToken(userWithToken.accessToken, userWithToken.refreshToken)
            tokenManager.saveUserEmail(userWithToken.user.email)
            userWithToken
        }
    )

    // READ
    override fun getLogInState(): Flow<DataResource<Boolean>> = resourceFactory.local(
        dataAction = {
            tokenManager.loadAccessToken()?.isNotEmpty() == true
        }
    )

    override fun getMyProfileInfo(): Flow<DataResource<User>> = resourceFactory.remote(
        dataAction = { userRemoteDataSource.getMyProfileInfo() }
    )

    // UPDATE
    override fun logOut(): Flow<DataResource<Unit>> = resourceFactory.remote(
        dataAction = {
            userRemoteDataSource.logOut()
            tokenManager.deleteToken()
        }
    )

    override fun updateProfileInfo(user: User): Flow<DataResource<Unit>> = resourceFactory.remote(
        dataAction = { userRemoteDataSource.updateProfileInfo(user.toData()) }
    )

    // DELETE
    override fun deleteUser(): Flow<DataResource<Unit>> = resourceFactory.remote(
        dataAction = {
            userRemoteDataSource.deleteUser()
            tokenManager.deleteToken()
        }
    )
}