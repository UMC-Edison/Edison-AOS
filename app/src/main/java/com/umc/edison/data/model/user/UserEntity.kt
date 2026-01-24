package com.umc.edison.data.model.user

import com.umc.edison.data.model.DataMapper
import com.umc.edison.domain.model.user.User

data class UserEntity(
    val id: Long? = null,
    val nickname: String?,
    val profileImage: String?,
    val email: String
) : DataMapper<User> {
    override fun toDomain(): User {
        return User(
            id = id,
            nickname = nickname,
            profileImage = profileImage,
            email = email
        )
    }
}

fun User.toData(): UserEntity {
    return UserEntity(
        id = id,
        nickname = nickname,
        profileImage = profileImage,
        email = email
    )
}