package com.umc.edison.remote.model.bubble

import com.google.gson.annotations.SerializedName

data class TrashBubbleResponse(
    @SerializedName("localIdx") val id: Int,
    @SerializedName("trashed") val trashed: Boolean
)
