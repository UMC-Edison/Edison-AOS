package com.umc.edison.remote.model.login

import com.google.gson.annotations.SerializedName

data class SignUpRequest (
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("identities")
    val identity: List<SetIdentityKeywordRequest>
)
