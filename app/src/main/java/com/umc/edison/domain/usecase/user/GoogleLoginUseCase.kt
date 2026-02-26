package com.umc.edison.domain.usecase.user

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.user.User
import com.umc.edison.domain.repository.BubbleRepository
import com.umc.edison.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val bubbleRepository: BubbleRepository
) {
    operator fun invoke(idToken: String): Flow<DataResource<User>> =
        userRepository.googleLogin(idToken).onEach { resource ->
            if (resource is DataResource.Success) {
                val userEmail = resource.data.email
                bubbleRepository.linkGuestBubblesToUser(userEmail)
            }
        }
}