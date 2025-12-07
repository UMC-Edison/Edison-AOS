package com.umc.edison.presentation.login

data class MakeNickNameState(
    val nickname: String = "",
) {
    companion object {
        val DEFAULT = MakeNickNameState()
    }
}
