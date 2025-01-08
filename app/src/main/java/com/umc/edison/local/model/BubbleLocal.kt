package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.umc.edison.data.model.BubbleEntity
import java.util.Date

@Entity
data class BubbleLocal(
    val title: String?,
    val content: String?,
    val date: Date,
    val mainImage: String?,
    val images: List<String>,
    var isDeleted: Boolean = false,
    var isSynced: Boolean = false,
) : LocalMapper<BubbleEntity> {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun toData(): BubbleEntity = BubbleEntity(
        id = id,
        title = title,
        content = content,
        mainImage = mainImage,
        images = images,
        labels = emptyList(),
        date = date.toString(),
    )
}
