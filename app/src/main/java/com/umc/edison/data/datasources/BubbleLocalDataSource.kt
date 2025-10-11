package com.umc.edison.data.datasources

import com.umc.edison.data.model.bubble.BubbleEntity

interface BubbleLocalDataSource {
    // CREATE
    suspend fun addBubbles(bubbles: List<BubbleEntity>)
    suspend fun addBubble(bubble: BubbleEntity) : BubbleEntity

    // READ
    suspend fun getAllActiveBubbles(): List<BubbleEntity>
    suspend fun getAllRecentBubbles(dayBefore: Int): List<BubbleEntity>
    suspend fun getAllTrashedBubbles(): List<BubbleEntity>
    suspend fun getActiveBubble(id: String): BubbleEntity
    suspend fun getRawBubble(id: String): BubbleEntity
    suspend fun getBubblesByLabelId(labelId: String): List<BubbleEntity>
    suspend fun getBubblesWithoutLabel(): List<BubbleEntity>
    suspend fun getSearchBubbleResults(query: String): List<BubbleEntity>
    suspend fun getUnSyncedBubbles(): List<BubbleEntity>

    // UPDATE
    suspend fun updateBubbles(bubbles: List<BubbleEntity>)
    suspend fun updateBubble(bubble: BubbleEntity, isSynced: Boolean = false) : BubbleEntity
    suspend fun markAsSynced(bubble: BubbleEntity)
    suspend fun syncBubbles(bubbles: List<BubbleEntity>)

    // DELETE
    suspend fun deleteBubbles(bubbles: List<BubbleEntity>)
}