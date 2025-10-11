package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.room.RoomConstant

@Dao
interface BubbleDao : BaseSyncDao<BubbleLocal> {
    // READ
    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE is_deleted = 0 AND is_trashed = 0")
    suspend fun getAllActiveBubbles(): List<BubbleLocal>

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE is_deleted = 0 AND is_trashed = 0 AND created_at >= :dayBefore")
    suspend fun getAllRecentBubbles(dayBefore: Long): List<BubbleLocal>

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE is_trashed = 1 AND is_deleted = 0")
    suspend fun getAllTrashedBubbles(): List<BubbleLocal>

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE id = :bubbleId AND is_deleted = 0 AND is_trashed = 0")
    suspend fun getActiveBubbleById(bubbleId: String): BubbleLocal?

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE id = :bubbleId")
    suspend fun getRawBubbleById(bubbleId: String): BubbleLocal?

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE id IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE label_id = :labelId) AND is_deleted = 0 AND is_trashed = 0")
    suspend fun getBubblesByLabelId(labelId: String): List<BubbleLocal>

    @Query(
        """
        SELECT DISTINCT b.* FROM ${RoomConstant.Table.BUBBLE} b
        LEFT JOIN ${RoomConstant.Table.BUBBLE_LABEL} bl ON b.id = bl.bubble_id
        LEFT JOIN ${RoomConstant.Table.LABEL} l ON bl.label_id = l.id
        WHERE 
            (b.title LIKE '%' || :query || '%' 
            OR b.content LIKE '%' || :query || '%' 
            OR l.name LIKE '%' || :query || '%')
            AND b.is_deleted = 0 
            AND b.is_trashed = 0
        """
    )
    suspend fun getSearchBubbles(query: String): List<BubbleLocal>

    @Query("SELECT * FROM ${RoomConstant.Table.BUBBLE} WHERE id NOT IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL}) AND is_deleted = 0 AND is_trashed = 0")
    suspend fun getBubblesWithoutLabel(): List<BubbleLocal>

    // DELETE
    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE} WHERE id IN (:ids)")
    suspend fun deleteBubbles(ids: List<String>)

    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE}")
    suspend fun deleteAllBubbles()
}