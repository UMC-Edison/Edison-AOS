package com.umc.edison.presentation.mypage

import android.net.Uri
import com.umc.edison.presentation.model.UserModel

data class EditProfileState(
    val user: UserModel,
    val selectedImages: List<Uri>
) {
    companion object {
        val DEFAULT = EditProfileState(
            user = UserModel.DEFAULT,
            selectedImages = emptyList()
        )
    }
}
