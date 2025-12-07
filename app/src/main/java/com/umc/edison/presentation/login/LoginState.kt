package com.umc.edison.presentation.login

import com.umc.edison.presentation.model.UserModel

data class LoginState(
    val user: UserModel?,
    val pendingGoogleIdToken: String?
) {
    companion object {
        val DEFAULT = LoginState(
            user = null,
            pendingGoogleIdToken = null
        )
    }
}
