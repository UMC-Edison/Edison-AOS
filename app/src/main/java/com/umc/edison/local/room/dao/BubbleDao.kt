package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.room.RoomConstant

@Dao
interface BubbleDao : BaseSyncDao<BubbleLocal> {

    @Query("UPDATE ${RoomConstant.Table.BUBBLE} SET user_email = :newUserEmail WHERE user_email IS NULL")
    suspend fun linkGuestBubblesToUser(newUserEmail: String)

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_deleted = 0 AND is_trashed = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getAllActiveBubbles(userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_deleted = 0 AND is_trashed = 0 
        AND created_at >= :dayBefore
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getAllRecentBubbles(dayBefore: Long, userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_trashed = 1 AND is_deleted = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getAllTrashedBubbles(userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id = :bubbleId AND is_deleted = 0 AND is_trashed = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getActiveBubbleById(bubbleId: String, userEmail: String?): BubbleLocal?

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id = :bubbleId
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getRawBubbleById(bubbleId: String, userEmail: String?): BubbleLocal?

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE label_id = :labelId) 
        AND is_deleted = 0 AND is_trashed = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getBubblesByLabelId(labelId: String, userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT DISTINCT b.* FROM ${RoomConstant.Table.BUBBLE} b
        LEFT JOIN ${RoomConstant.Table.BUBBLE_LABEL} bl ON b.id = bl.bubble_id
        LEFT JOIN ${RoomConstant.Table.LABEL} l ON bl.label_id = l.id
        WHERE 
            (b.title LIKE '%' || :query || '%' 
            OR b.content LIKE '%' || :query || '%' 
            OR l.name LIKE '%' || :query || '%')
            AND b.is_deleted = 0 
            AND b.is_trashed = 0
            AND ((:userEmail IS NULL AND b.user_email IS NULL) OR (b.user_email = :userEmail))
    """)
    suspend fun getSearchBubbles(query: String, userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id NOT IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL}) 
        AND is_deleted = 0 AND is_trashed = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getBubblesWithoutLabel(userEmail: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id IN (:bubbleIds) AND is_deleted = 0 AND is_trashed = 0
        AND ((:userEmail IS NULL AND user_email IS NULL) OR (user_email = :userEmail))
    """)
    suspend fun getActiveBubblesByIds(bubbleIds: List<String>, userEmail: String?): List<BubbleLocal>

    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE} WHERE id IN (:ids)")
    suspend fun deleteBubbles(ids: List<String>)

    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE}")
    suspend fun deleteAllBubbles()
}