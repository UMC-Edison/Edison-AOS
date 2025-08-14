package com.umc.edison.remote.model.sync

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.SyncBubbleEntity
import com.umc.edison.remote.model.RemoteMapper
import com.umc.edison.remote.model.parseIso8601ToDate

data class SyncBubbleResponse(
    @SerializedName("localIdx") val bubbleId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("mainImageUrl") val mainImageUrl: String,
    @SerializedName("labels") val labels: List<SyncBubbleLabelResponse> = emptyList(),
    @SerializedName("backlinkIdxs") val backlinkIds: List<String> = emptyList(),
    @SerializedName("linkedBubbleIdx") val linkedBubbleId: String?,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("isTrashed") val isTrashed: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("deletedAt") val deletedAt: String?
) : RemoteMapper<SyncBubbleEntity> {
    override fun toData(): SyncBubbleEntity = SyncBubbleEntity(
        id = bubbleId,
        title = title,
        content = content,
        mainImage = mainImageUrl,
        labelIds = labels.map { it.id },
        backLinkIds = backlinkIds,
        linkedBubbleId = linkedBubbleId,
        isDeleted = isDeleted,
        isTrashed = isTrashed,
        createdAt = parseIso8601ToDate(createdAt),
        updatedAt = parseIso8601ToDate(updatedAt),
        deletedAt = deletedAt?.let { parseIso8601ToDate(it) },
    )

    data class SyncBubbleLabelResponse(
        @SerializedName("localIdx") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("color") val color: Int
    )
}
