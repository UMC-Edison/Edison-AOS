package com.umc.edison.remote.model.mypage

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.user.UserEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetProfileInfoResponse(
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImg") val profileImg: String,
) : RemoteMapper<UserEntity> {
    override fun toData(): UserEntity = UserEntity(
        email = email,
        nickname = nickname,
        profileImage = profileImg,
    )
}
