package com.umc.edison.presentation.login

import com.umc.edison.presentation.model.UserModel

sealed interface GoogleLoginState {
    data object Idle : GoogleLoginState
    data object Loading : GoogleLoginState
    data class Success(val userModel: UserModel) : GoogleLoginState
    data class MemberNotFound(val idToken: String) : GoogleLoginState
    data class Failure(
        val message: String = ERROR_MESSAGE_UNKNOWN
    ) : GoogleLoginState

    companion object {
        const val ERROR_CODE_MEMBER_NOT_FOUND = "MEMBER4001"
        const val ERROR_MESSAGE_LOGIN_FAILED = "LOGIN_ERROR"
        const val ERROR_MESSAGE_CANCELLED = "사용자가 로그인 창을 닫았습니다."
        const val ERROR_MESSAGE_UNKNOWN = "알 수 없는 로그인 오류 발생"
        const val ERROR_MESSAGE_INVALID_TOKEN = "잘못된 ID Token 응답을 받았습니다."

        val DEFAULT = Idle
    }
}