package com.umc.edison.presentation.model

import com.umc.edison.domain.model.Bubble

data class BubbleModel(
    val id: Int,
    val title: String? = null,
    val content: String? = null,
    val mainImage: String? = null,
    val images: List<String>,
    val labels: List<LabelModel>,
    val date: String,
)

fun Bubble.toPresentation(): BubbleModel =
    BubbleModel(id, title, content, mainImage, images, labels.map { it.toPresentation() }, date)

fun List<Bubble>.toPresentation(): List<BubbleModel> = map { it.toPresentation() }
