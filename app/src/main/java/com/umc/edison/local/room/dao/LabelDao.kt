package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.model.LabelWithBubbleId
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

    @Query(
        "SELECT l.*, bl.bubble_id FROM ${RoomConstant.Table.LABEL} l " +
                "INNER JOIN ${RoomConstant.Table.BUBBLE_LABEL} bl ON l.id = bl.label_id " +
                "WHERE bl.bubble_id IN (:bubbleIds) AND l.is_deleted = 0"
    )
    suspend fun getAllActiveLabelsByBubbleIds(bubbleIds: List<String>): List<LabelWithBubbleId>

    @Query("SELECT * FROM ${RoomConstant.Table.LABEL} WHERE id IN (:labelIds)")
    suspend fun getLabelsByIds(labelIds: List<String>): List<LabelLocal>

    // DELETE
    @Query("DELETE FROM ${RoomConstant.Table.LABEL}")
    suspend fun deleteAllLabels()
}
