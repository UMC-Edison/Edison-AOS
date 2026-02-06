package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.room.RoomConstant

@Dao
interface BubbleDao : BaseSyncDao<BubbleLocal> {

    // user_id가 NULL인 버블들을 현재 로그인한 user_id로 일괄 업데이트
    @Query("UPDATE ${RoomConstant.Table.BUBBLE} SET user_id = :newUserId WHERE user_id IS NULL")
    suspend fun linkGuestBubblesToUser(newUserId: String)

    // 현재 로그인한 사용자의 버블만 조회
    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_deleted = 0 AND is_trashed = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getAllActiveBubbles(userId: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_deleted = 0 AND is_trashed = 0 
        AND created_at >= :dayBefore
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getAllRecentBubbles(dayBefore: Long, userId: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE is_trashed = 1 AND is_deleted = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getAllTrashedBubbles(userId: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id = :bubbleId AND is_deleted = 0 AND is_trashed = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getActiveBubbleById(bubbleId: String, userId: String?): BubbleLocal?

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id = :bubbleId
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getRawBubbleById(bubbleId: String, userId: String?): BubbleLocal?

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE label_id = :labelId) 
        AND is_deleted = 0 AND is_trashed = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getBubblesByLabelId(labelId: String, userId: String?): List<BubbleLocal>

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
            AND ((:userId IS NULL AND b.user_id IS NULL) OR (b.user_id = :userId))
    """)
    suspend fun getSearchBubbles(query: String, userId: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id NOT IN (SELECT bubble_id FROM ${RoomConstant.Table.BUBBLE_LABEL}) 
        AND is_deleted = 0 AND is_trashed = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getBubblesWithoutLabel(userId: String?): List<BubbleLocal>

    @Query("""
        SELECT * FROM ${RoomConstant.Table.BUBBLE} 
        WHERE id IN (:bubbleIds) AND is_deleted = 0 AND is_trashed = 0
        AND ((:userId IS NULL AND user_id IS NULL) OR (user_id = :userId))
    """)
    suspend fun getActiveBubblesByIds(bubbleIds: List<String>, userId: String?): List<BubbleLocal>

    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE} WHERE id IN (:ids)")
    suspend fun deleteBubbles(ids: List<String>)

    @Query("DELETE FROM ${RoomConstant.Table.BUBBLE}")
    suspend fun deleteAllBubbles()
}