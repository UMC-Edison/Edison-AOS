package com.umc.edison.presentation.mypage

import com.umc.edison.presentation.model.UserModel

data class EditProfileState(
    val user: UserModel,
) {
    companion object {
        val DEFAULT = EditProfileState(
            user = UserModel.DEFAULT,
        )
    }
}
