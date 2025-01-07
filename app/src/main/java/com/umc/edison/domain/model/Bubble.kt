package com.umc.edison.domain.model

data class Bubble(
    val id: Int,
    val title: String,
    val content: String,
    val mainImage: String,
    val images: List<String>,
    val labels: List<Label>,
)
