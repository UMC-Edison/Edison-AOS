package com.umc.edison.remote.model.label

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.label.LabelEntity
import com.umc.edison.remote.model.RemoteMapper

data class LabelResponse(
    @SerializedName("localIdx") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("color") val color: Int
) : RemoteMapper<LabelEntity> {
    override fun toData(): LabelEntity {
        return LabelEntity(
            id = id,
            name = name,
            color = Color(color),
        )
    }
}
