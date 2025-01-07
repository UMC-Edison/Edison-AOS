package com.umc.edison.domain.model

data class Bubble(
    val id: Int,
    val title: String? = null,
    val content: String? = null,
    val mainImage: String? = null,
    val images: List<String>,
    val labels: List<Label>,
    val date: String,
)
