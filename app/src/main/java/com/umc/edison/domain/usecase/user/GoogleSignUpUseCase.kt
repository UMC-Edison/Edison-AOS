package com.umc.edison.domain.usecase.user

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.identity.Identity
import com.umc.edison.domain.model.user.User
import com.umc.edison.domain.repository.BubbleRepository
import com.umc.edison.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GoogleSignUpUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val bubbleRepository: BubbleRepository
) {
    operator fun invoke(
        idToken: String,
        nickname: String,
        identities: List<Identity>
    ): Flow<DataResource<User>> =
        userRepository.googleSignUp(idToken, nickname, identities).onEach { resource ->
            if (resource is DataResource.Success) {
                val userEmail = resource.data.email
                bubbleRepository.linkGuestBubblesToUser(userEmail)
            }
        }
}