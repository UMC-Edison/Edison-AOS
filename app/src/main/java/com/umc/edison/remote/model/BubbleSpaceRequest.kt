package com.umc.edison.remote.model

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.data.model.LabelEntity

data class BubbleRequest(
    @SerializedName("title")
    val title: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("image_urls")
    val images: List<String>,
    @SerializedName("main_image_url")
    val mainImage: String?,
    @SerializedName("labels")
    val labels: List<LabelRequest>,
)

fun BubbleEntity.toRemote(): BubbleRequest = BubbleRequest(
    title = title,
    content = content,
    images = images,
    mainImage = mainImage,
    labels = labels.map { it.toRemote() },
)

data class LabelRequest(
    @SerializedName("label_name")
    val name: String,
    @SerializedName("label_color")
    val color: String,
)

fun LabelEntity.toRemote(): LabelRequest = LabelRequest(
    name = name,
    color = color,
)