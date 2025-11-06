package com.umc.edison.remote.model.sync

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.remote.model.toIso8601String

data class SyncBubbleRequest(
    @SerializedName("localIdx") val bubbleId: String,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("mainImageUrl") val mainImageUrl: String?,
    @SerializedName("backlinkIds") val backLinkIds: List<String>,
    @SerializedName("labelIdxs") val labelIds: List<String>,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("isTrashed") val isTrashed: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("deletedAt") val deletedAt: String?
)

fun BubbleEntity.toSyncBubbleRequest(): SyncBubbleRequest = SyncBubbleRequest(
    bubbleId = id,
    title = title,
    content = content,
    mainImageUrl = mainImage,
    labelIds = labels.map { it.id },
    backLinkIds = backLinks.map { it.id },
    isDeleted = isDeleted,
    isTrashed = isTrashed,
    createdAt = createdAt.toIso8601String(),
    updatedAt = updatedAt.toIso8601String(),
    deletedAt = deletedAt?.toIso8601String()
)
