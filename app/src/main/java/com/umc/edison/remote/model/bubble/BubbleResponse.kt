package com.umc.edison.remote.model.bubble

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.remote.model.RemoteMapper
import com.umc.edison.remote.model.label.LabelResponse
import com.umc.edison.remote.model.parseIso8601ToDate

data class BubbleResponse(
    @SerializedName("localIdx") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("mainImageUrl") val mainImageUrl: String?,
    @SerializedName("labels") val labels: List<LabelResponse>,
    @SerializedName("backlinkIdxs") val backlinkIds: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) : RemoteMapper<BubbleEntity> {
    override fun toData(): BubbleEntity {
        return BubbleEntity(
            id = id,
            title = title,
            content = content,
            mainImage = mainImageUrl,
            labels = labels.map { it.toData() },
            backLinks = backlinkIds.map { BubbleEntity(id = it, title = null, content = null, mainImage = null, labels = emptyList(), backLinks = emptyList(), linkedBubble = null) },
            linkedBubble = null,
            createdAt = parseIso8601ToDate(createdAt),
            updatedAt = parseIso8601ToDate(updatedAt)
        )
    }
}

fun List<BubbleResponse>.toData(): List<BubbleEntity> = map { it.toData() }
