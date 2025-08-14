package com.umc.edison.data.model.s3

import com.umc.edison.data.model.DataMapper
import com.umc.edison.domain.model.s3.PresignedUrl

data class PresignedUrlEntity(
    val key: String,
    val presignedUrl: String
) : DataMapper<PresignedUrl> {
    override fun toDomain(): PresignedUrl =
        PresignedUrl(key = key, url = presignedUrl)
}