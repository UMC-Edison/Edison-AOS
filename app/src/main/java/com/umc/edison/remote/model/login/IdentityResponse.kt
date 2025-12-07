package com.umc.edison.remote.model.login

import com.google.gson.annotations.SerializedName

data class IdentityResponse(
    @SerializedName("category")
    val category: String,
    @SerializedName("keywords")
    val keywords: List<Int>
)
