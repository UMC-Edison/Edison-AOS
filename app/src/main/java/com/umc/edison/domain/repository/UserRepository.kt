package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.identity.Identity
import com.umc.edison.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // CREATE
    fun googleLogin(idToken:String): Flow<DataResource<User>>
    fun googleSignUp(
        idToken: String,
        nickname: String,
        identity: List<Identity>
    ): Flow<DataResource<User>>

    // READ
    fun getLogInState(): Flow<DataResource<Boolean>>
    fun getMyProfileInfo(): Flow<DataResource<User>>

    // UPDATE
    fun logOut(): Flow<DataResource<Unit>>
    fun updateProfileInfo(user: User): Flow<DataResource<Unit>>

    // DELETE
    fun deleteUser(): Flow<DataResource<Unit>>
}