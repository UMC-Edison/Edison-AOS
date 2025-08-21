package com.umc.edison.data.datasources

import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
import com.umc.edison.data.model.bubble.SyncBubbleEntity

interface BubbleRemoteDataSource {
    // CREATE
    suspend fun addBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun addBubble(bubble: BubbleEntity): BubbleEntity

    // READ
    suspend fun getAllClusteredBubbles(): List<PositionBubbleEntity>
    suspend fun getAllBubbles(): List<BubbleEntity>
    suspend fun getKeywordBubbles(keyword: String): List<SimilarityBubbleEntity>

    // UPDATE
    suspend fun recoverBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun updateBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun updateBubble(bubble: BubbleEntity): BubbleEntity
    suspend fun syncBubble(bubble: BubbleEntity): SyncBubbleEntity

    // DELETE
    suspend fun deleteBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun trashBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
}