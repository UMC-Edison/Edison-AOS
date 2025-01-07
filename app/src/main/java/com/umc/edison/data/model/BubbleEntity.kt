package com.umc.edison.data.model

import com.umc.edison.domain.model.Bubble
import com.umc.edison.domain.model.Label

data class BubbleEntity(
    val id: Int,
    val title: String,
    val content: String,
    val mainImage: String,
    val images: List<String>,
    val labels: List<Label>,
) : DataMapper<Bubble> {
    override fun toDomain(): Bubble = Bubble(
        id = id,
        title = title,
        content = content,
        mainImage = mainImage,
        images = images,
        labels = labels,
    )
}