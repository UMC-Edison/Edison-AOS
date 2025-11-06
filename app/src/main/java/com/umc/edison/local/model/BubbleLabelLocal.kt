package com.umc.edison.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    indices = [Index(value = ["bubble_id", "label_id"], unique = true), Index(value = ["label_id"]), Index(value = ["bubble_id"])],
    foreignKeys = [
        ForeignKey(
            entity = BubbleLocal::class,
            parentColumns = ["id"],
            childColumns = ["bubble_id"],
            onDelete = ForeignKey.CASCADE // Bubble 삭제 시 관련 BubbleLabel도 삭제
        ),
        ForeignKey(
            entity = LabelLocal::class,
            parentColumns = ["id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE // Label 삭제 시 관련 BubbleLabel도 삭제
        )
    ]
)
data class BubbleLabelLocal(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    @ColumnInfo(name = "bubble_id") val bubbleId: String,
    @ColumnInfo(name = "label_id") val labelId: String,
    @ColumnInfo(name = "created_at") override var createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at") override var updatedAt: Date = Date(),
) : BaseLocal

// 배치 조회를 위한 데이터 클래스
data class BubbleLabelRelation(
    @ColumnInfo(name = "bubble_id") val bubbleId: String,
    @ColumnInfo(name = "label_id") val labelId: String
)
