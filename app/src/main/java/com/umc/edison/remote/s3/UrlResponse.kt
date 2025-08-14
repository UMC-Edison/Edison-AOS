package com.umc.edison.remote.s3

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.s3.PresignedUrlEntity
import com.umc.edison.remote.model.RemoteMapper

data class UrlResponse(
    @SerializedName("key") val key: String,
    @SerializedName("presignedUrl") val presignedUrl: String,
) : RemoteMapper<PresignedUrlEntity> {
    override fun toData(): PresignedUrlEntity =
        PresignedUrlEntity(key = key, presignedUrl = presignedUrl)
}