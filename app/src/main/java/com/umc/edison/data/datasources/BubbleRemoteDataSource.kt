package com.umc.edison.data.datasources

import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity

interface BubbleRemoteDataSource {
    // CREATE
    suspend fun addBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun addBubble(bubble: BubbleEntity): BubbleEntity

    // READ
    suspend fun getAllClusteredBubbles(): List<PositionBubbleEntity>

    // UPDATE
    suspend fun recoverBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun updateBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun updateBubble(bubble: BubbleEntity): BubbleEntity

    // DELETE
    suspend fun deleteBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
    suspend fun trashBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity>
}