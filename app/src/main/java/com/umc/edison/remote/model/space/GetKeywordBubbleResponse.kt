package com.umc.edison.remote.model.space

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetKeywordBubbleResponse(
    @SerializedName("localIdx") val localIdx: String,
    @SerializedName("similarity") val similarity: Float
) : RemoteMapper<SimilarityBubbleEntity> {
    override fun toData(): SimilarityBubbleEntity =
        SimilarityBubbleEntity(
            id = localIdx,
            similarity = similarity
        )
}