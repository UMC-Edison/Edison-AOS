package com.umc.edison.domain.usecase.user

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.identity.Identity
import com.umc.edison.domain.model.user.User
import com.umc.edison.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoogleSignUpUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(
        idToken: String,
        nickname: String,
        identities: List<Identity>
    ): Flow<DataResource<User>> =
        userRepository.googleSignUp(idToken, nickname, identities)
}