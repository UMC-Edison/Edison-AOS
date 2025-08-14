package com.umc.edison.remote.model.bubble

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.BubbleEntity

data class AddBubbleRequest(
    @SerializedName("localIdx") val localIdx: String,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("mainImageUrl") val mainImageUrl: String?,
    @SerializedName("labelIdxs") val labelIds: List<String>,
    @SerializedName("backlinkIds") val backlinkIds: List<String>
)

fun BubbleEntity.toAddBubbleRequest(): AddBubbleRequest = AddBubbleRequest(
    localIdx = id,
    title = title,
    content = content,
    mainImageUrl = mainImage ?: "",
    labelIds = labels.map { it.id },
    backlinkIds = backLinks.map { it.id }
)