package com.umc.edison.remote.model.label

import androidx.compose.ui.graphics.toArgb
import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.label.LabelEntity

data class AddLabelRequest(
    @SerializedName("localIdx") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("color") val color: Int
)

fun LabelEntity.toAddLabelRequest(): AddLabelRequest {
    return AddLabelRequest(
        id = id,
        name = name,
        color = color.toArgb()
    )
}
