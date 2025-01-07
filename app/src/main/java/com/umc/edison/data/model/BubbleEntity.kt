package com.umc.edison.data.model

import com.umc.edison.domain.model.Bubble

data class BubbleEntity(
    val id: Int,
    val title: String? = null,
    val content: String? = null,
    val mainImage: String? = null,
    val images: List<String>,
    val labels: List<LabelEntity>,
    val date: String,
) : DataMapper<Bubble> {
    override fun toDomain(): Bubble = Bubble(
        id = id,
        title = title,
        content = content,
        mainImage = mainImage,
        images = images,
        labels = labels.map { it.toDomain() },
        date = date,
    )
}