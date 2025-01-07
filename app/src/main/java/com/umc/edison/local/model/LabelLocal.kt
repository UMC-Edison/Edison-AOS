package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.umc.edison.data.model.LabelEntity

@Entity
data class LabelLocal(
    val name: String,
    val color: String,
    var isDeleted: Boolean,
    var isSynced: Boolean,
) : LocalMapper<LabelEntity> {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun toData(): LabelEntity = LabelEntity(
        id = id,
        name = name,
        color = color,
    )
}
