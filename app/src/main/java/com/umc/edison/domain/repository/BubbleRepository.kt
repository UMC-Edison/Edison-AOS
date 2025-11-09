package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.bubble.Bubble
import com.umc.edison.domain.model.bubble.ClusteredBubble
import com.umc.edison.domain.model.bubble.KeywordBubble
import kotlinx.coroutines.flow.Flow

interface BubbleRepository {
    // CREATE
    fun addBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>>
    fun addBubble(bubble: Bubble): Flow<DataResource<Bubble>>

    // READ
    fun getAllActiveBubbles(): Flow<DataResource<List<Bubble>>>
    fun getAllClusteredBubbles(): Flow<DataResource<List<ClusteredBubble>>>
    fun getAllRecentBubbles(dayBefore: Int): Flow<DataResource<List<Bubble>>>
    fun getAllTrashedBubbles(): Flow<DataResource<List<Bubble>>>
    fun getActiveBubble(id: String): Flow<DataResource<Bubble>>
    fun getBubblesByLabel(labelId: String): Flow<DataResource<List<Bubble>>>
    fun getBubblesWithoutLabel(): Flow<DataResource<List<Bubble>>>
    fun searchBubbles(query: String): Flow<DataResource<List<Bubble>>>
    fun getKeywordBubbles(keyword: String) : Flow<DataResource<List<KeywordBubble>>>

    // UPDATE
    fun recoverBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>>
    fun updateBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>>
    fun updateBubble(bubble: Bubble): Flow<DataResource<Bubble>>

    // DELETE
    fun deleteBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>>
    fun trashBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>>
}