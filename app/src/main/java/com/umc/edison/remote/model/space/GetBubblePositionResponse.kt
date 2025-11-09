package com.umc.edison.remote.model.space

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetBubblePositionResponse(
    @SerializedName("localIdx") val id : String,
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("group") val group: Int
) : RemoteMapper<PositionBubbleEntity> {
    override fun toData(): PositionBubbleEntity =
        PositionBubbleEntity(
            id = id,
            x = x,
            y = y,
            group = group
        )
}
