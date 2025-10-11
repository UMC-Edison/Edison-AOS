package com.umc.edison.data.datasources

import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
import com.umc.edison.data.model.bubble.SyncBubbleEntity

interface BubbleRemoteDataSource {
    // READ
    suspend fun getAllClusteredBubbles(): List<PositionBubbleEntity>
    suspend fun getAllBubbles(): List<BubbleEntity>
    suspend fun getKeywordBubbles(keyword: String): List<SimilarityBubbleEntity>

    // UPDATE
    suspend fun syncBubble(bubble: BubbleEntity): SyncBubbleEntity
}
