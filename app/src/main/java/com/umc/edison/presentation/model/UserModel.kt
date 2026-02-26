package com.umc.edison.presentation.model

import com.umc.edison.domain.model.user.User

data class UserModel(
    val nickname: String?,
    val profileImage: String?,
    val email: String,
) {
    companion object {
        val DEFAULT = UserModel(
            nickname = null,
            profileImage = null,
            email = "",
        )
    }

    fun toDomain(): User {
        return User(
            id = null,
            nickname = nickname,
            profileImage = profileImage,
            email = email,
        )
    }
}

fun User.toPresentation(): UserModel {
    return UserModel(
        nickname = nickname,
        profileImage = profileImage,
        email = email,
    )
}
