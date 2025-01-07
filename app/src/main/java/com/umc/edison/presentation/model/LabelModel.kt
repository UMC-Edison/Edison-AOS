package com.umc.edison.presentation.model

import com.umc.edison.domain.model.Label

data class LabelModel(
    val id: Int,
    val name: String,
)

fun Label.toPresentation(): LabelModel =
    LabelModel(id, name)

fun List<Label>.toPresentation(): List<LabelModel> = map { it.toPresentation() }