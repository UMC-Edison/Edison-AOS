package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
import com.umc.edison.data.model.bubble.SyncBubbleEntity
import com.umc.edison.remote.api.BubbleApiService
import com.umc.edison.remote.api.BubbleSpaceApiService
import com.umc.edison.remote.api.SyncApiService
import com.umc.edison.remote.model.bubble.toAddBubbleRequest
import com.umc.edison.remote.model.bubble.toData
import com.umc.edison.remote.model.bubble.toUpdateBubbleRequest
import com.umc.edison.remote.model.sync.toSyncBubbleRequest
import javax.inject.Inject

class BubbleRemoteDataSourceImpl @Inject constructor(
    private val bubbleApiService: BubbleApiService,
    private val bubbleSpaceApiService: BubbleSpaceApiService,
    private val syncApiService: SyncApiService,
) : BubbleRemoteDataSource {
    // CREATE
    override suspend fun addBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        val results = mutableListOf<BubbleEntity>()
        bubbles.map {
            results.add(addBubble(it))
        }

        return results
    }

    override suspend fun addBubble(bubble: BubbleEntity): BubbleEntity {
        return bubbleApiService.addBubble(bubble.toAddBubbleRequest()).data.toData()
    }

    // READ
    override suspend fun getAllClusteredBubbles(): List<PositionBubbleEntity> {
        return bubbleSpaceApiService.getBubblePosition().data.map { it.toData() }
    }

    override suspend fun getAllBubbles(): List<BubbleEntity> {
        return bubbleApiService.getAllBubbles().data.toData()
    }

    override suspend fun getKeywordBubbles(keyword: String): List<SimilarityBubbleEntity> {
        return bubbleSpaceApiService.getKeywordBubbles(keyword).data.map { it.toData() }
    }

    // UPDATE
    override suspend fun recoverBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        val results = mutableListOf<BubbleEntity>()
        bubbles.map {
            results.add(recoverBubble(it))
        }
        return results
    }

    private suspend fun recoverBubble(bubble: BubbleEntity): BubbleEntity {
        bubbleApiService.restoreBubble(bubble.id)

        return bubble.copy(isTrashed = false)
    }

    override suspend fun updateBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        val results = mutableListOf<BubbleEntity>()
        bubbles.map {
            results.add(updateBubble(it))
        }
        return results
    }

    override suspend fun updateBubble(bubble: BubbleEntity): BubbleEntity {
        return bubbleApiService.updateBubble(bubble.id, bubble.toUpdateBubbleRequest()).data.toData()
    }

    override suspend fun syncBubble(bubble: BubbleEntity): SyncBubbleEntity {
        return syncApiService.syncBubble(bubble.toSyncBubbleRequest()).data.toData()
    }

    // DELETE
    override suspend fun deleteBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        val results = mutableListOf<BubbleEntity>()
        bubbles.map {
            results.add(deleteBubble(it))
        }
        return results
    }

    private suspend fun deleteBubble(bubble: BubbleEntity): BubbleEntity {
        bubbleApiService.deleteBubble(bubble.id)
        return bubble.copy(isDeleted = true)
    }

    override suspend fun trashBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        val results = mutableListOf<BubbleEntity>()
        bubbles.map {
            results.add(trashBubble(it))
        }
        return results
    }

    private suspend fun trashBubble(bubble: BubbleEntity): BubbleEntity {
        bubbleApiService.trashBubble(bubble.id)
        return bubble.copy(isTrashed = true)
    }
}