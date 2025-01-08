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
            onDelete = ForeignKey.CASCADE // Bubble 삭제 시 관련 BubbleLabel도 삭제
        ),
        ForeignKey(
            entity = LabelLocal::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE // Label 삭제 시 관련 BubbleLabel도 삭제
        )
    ]
)
data class BubbleLabelLocal(
    val bubbleId: Int,
    val labelId: Int,
    var isSynced: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
