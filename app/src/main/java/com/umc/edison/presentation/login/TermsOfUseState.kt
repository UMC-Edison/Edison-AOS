package com.umc.edison.presentation.login


data class TermsOfUseState(
    val fromSignUp: Boolean,
    val idToken: String,
) {
    companion object {
        val DEFAULT = TermsOfUseState(
            fromSignUp = false,
            idToken = "",
        )
    }
}
