package com.umc.edison.domain.model.bubble

import com.umc.edison.domain.model.label.Label
import com.umc.edison.presentation.model.ContentBlockModel
import java.util.Date
import java.util.LinkedList

data class Bubble(
    val id: String,
    val title: String?,
    val content: String?,
    val mainImage: String?,
    val labels: List<Label>,
    val backLinks: List<Bubble>,
    val linkedBubble: Bubble?,
    val date: Date,
)
