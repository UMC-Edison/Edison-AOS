package com.umc.edison.remote.model

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.data.model.LabelEntity

data class BubbleResponse(
    @SerializedName("bubble_id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("image_urls")
    val images: List<String>,
    @SerializedName("main_image_url")
    val mainImage: String?,
    @SerializedName("labels")
    val labels: List<LabelResponse>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
) : RemoteMapper<BubbleEntity> {
    override fun toData(): BubbleEntity = BubbleEntity(
        id = id,
        title = title,
        content = content,
        images = images,
        mainImage = mainImage,
        labels = labels.map { it.toData() },
        date = updatedAt,
    )
}

data class LabelResponse(
    @SerializedName("label_id")
    val id: Int,
    @SerializedName("label_name")
    val name: String,
    @SerializedName("label_color")
    val color: String,
) : RemoteMapper<LabelEntity> {
    override fun toData(): LabelEntity = LabelEntity(
        id = id,
        name = name,
        color = color,
    )
}