package com.umc.edison.domain.model.bubble

import com.umc.edison.domain.model.label.Label
import java.util.Date

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
