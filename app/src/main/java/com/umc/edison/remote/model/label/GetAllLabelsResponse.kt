package com.umc.edison.remote.model.label

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.label.LabelEntity
import com.umc.edison.remote.model.RemoteMapper
import com.umc.edison.remote.model.parseIso8601ToDate

data class GetAllLabelsResponse(
    @SerializedName("localIdx") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("color") val color: Int,
    @SerializedName("bubbleCount") val bubbleCount: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) : RemoteMapper<LabelEntity> {
    override fun toData(): LabelEntity {
        return LabelEntity(
            id = id,
            name = name,
            color = Color(color),
            createdAt = parseIso8601ToDate(createdAt),
            updatedAt = parseIso8601ToDate(updatedAt),
        )
    }
}

fun List<GetAllLabelsResponse>.toData(): List<LabelEntity> = map { it.toData() }