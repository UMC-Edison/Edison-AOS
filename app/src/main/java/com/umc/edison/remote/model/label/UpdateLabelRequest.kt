package com.umc.edison.remote.model.label

import androidx.compose.ui.graphics.toArgb
import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.label.LabelEntity

data class UpdateLabelRequest(
    @SerializedName("name") val name: String,
    @SerializedName("color") val color: Int
)

fun LabelEntity.toUpdateLabelRequest(): UpdateLabelRequest {
    return UpdateLabelRequest(
        name = name,
        color = color.toArgb()
    )
}
