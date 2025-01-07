package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BubbleLocal::class,
            parentColumns = ["id"],
            childColumns = ["bubbleId"],
            onDelete = ForeignKey.CASCADE // Bubble 삭제 시 관련 BubbleImage도 삭제
        ),
        ForeignKey(
            entity = ImageLocal::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE // Image 삭제 시 관련 BubbleImage도 삭제
        )
    ]
)
data class BubbleImageLocal(
    val bubbleId: Int,
    val imageId: Int,
    val isMainImage: Boolean = false,
    var isSynced: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
