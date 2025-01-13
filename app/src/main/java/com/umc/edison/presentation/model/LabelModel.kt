package com.umc.edison.presentation.model

import androidx.compose.ui.graphics.Color
import com.umc.edison.domain.model.Label

data class LabelModel(
    val id: Int,
    val name: String,
    val color: Color,
)

fun Label.toPresentation(): LabelModel {
    val stringToColor = Color(color.toInt())
    return LabelModel(id, name, stringToColor)
}

fun List<Label>.toPresentation(): List<LabelModel> = map { it.toPresentation() }