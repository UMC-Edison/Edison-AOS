package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.remote.api.BubbleSpaceApiService
import com.umc.edison.remote.api.S3ApiService
import javax.inject.Inject

class BubbleRemoteDataSourceImpl @Inject constructor(
    private val bubbleSpaceApiService: BubbleSpaceApiService
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
        TODO("Not yet implemented")
    }

    // READ
    override suspend fun getAllClusteredBubbles(): List<PositionBubbleEntity> {
        return bubbleSpaceApiService.getBubblePosition().data.map { it.toData() }
    }


    // UPDATE
    override suspend fun recoverBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun updateBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun updateBubble(bubble: BubbleEntity): BubbleEntity {
        TODO("Not yet implemented")
    }

    // DELETE
    override suspend fun deleteBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun trashBubbles(bubbles: List<BubbleEntity>): List<BubbleEntity> {
        TODO("Not yet implemented")
    }
}