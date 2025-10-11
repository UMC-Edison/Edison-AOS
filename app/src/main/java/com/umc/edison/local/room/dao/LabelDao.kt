package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.room.RoomConstant

@Dao
interface LabelDao : BaseSyncDao<LabelLocal> {
    // READ
    @Query("SELECT * FROM ${RoomConstant.Table.LABEL} WHERE is_deleted = 0")
    suspend fun getAllActiveLabels(): List<LabelLocal>

    @Query(
        "SELECT * FROM ${RoomConstant.Table.LABEL} " +
                "Where id IN (" +
                    "SELECT label_id FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE bubble_id = :bubbleId" +
                ") AND is_deleted = 0"
    )
    suspend fun getAllActiveLabelsByBubbleId(bubbleId: String): List<LabelLocal>

    @Query(
        "SELECT * FROM ${RoomConstant.Table.LABEL} " +
                "Where id IN (" +
                "SELECT label_id FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE bubble_id = :bubbleId" +
                ") AND is_deleted = 0"
    )
    suspend fun getAllRawLabelsByBubbleId(bubbleId: String): List<LabelLocal>

    @Query("SELECT * FROM ${RoomConstant.Table.LABEL} WHERE id = :labelId")
    suspend fun getLabelById(labelId: String): LabelLocal?

    // DELETE
    @Query("DELETE FROM ${RoomConstant.Table.LABEL}")
    suspend fun deleteAllLabels()
}