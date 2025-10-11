package com.umc.edison.local.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.umc.edison.data.model.label.LabelEntity
import java.util.Date
import java.util.UUID

@Entity
data class LabelLocal(
    @PrimaryKey
    @ColumnInfo(name = "id") override val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int,
    @ColumnInfo(name = "is_synced") override var isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted") override var isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at") override var createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at") override var updatedAt: Date = Date(),
    @ColumnInfo(name = "deleted_at") override var deletedAt: Date? = null,
) : LocalMapper<LabelEntity>, BaseSyncLocal {
    override fun toData(): LabelEntity = LabelEntity(
        id = uuid,
        name = name,
        color = Color(color),
        isDeleted = isDeleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}

fun LabelEntity.toLocal(): LabelLocal = LabelLocal(
    uuid = id,
    name = name,
    color = color.toArgb(),
    isSynced = isSynced,
    isDeleted = isDeleted,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

data class LabelWithBubbleId(
    @ColumnInfo(name = "id") val uuid: String,
    val name: String,
    val color: Int,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date,
    @ColumnInfo(name = "deleted_at") val deletedAt: Date?,
    @ColumnInfo(name = "bubble_id") val bubbleId: String
) {
    fun toLabelEntity(): LabelEntity = LabelEntity(
        id = uuid,
        name = name,
        color = Color(color),
        isDeleted = isDeleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
