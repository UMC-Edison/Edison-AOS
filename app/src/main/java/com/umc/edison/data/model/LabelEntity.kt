package com.umc.edison.data.model

import com.umc.edison.domain.model.Label

data class LabelEntity(
    val id: Int,
    val name: String,
    val color: String,
) : DataMapper<Label> {
    override fun toDomain(): Label = Label(
        id = id,
        name = name,
        color = color,
    )
}
