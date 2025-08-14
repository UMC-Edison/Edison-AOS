package com.umc.edison.domain.model.s3

data class PresignedUrl(
    val key: String,
    val url: String
)