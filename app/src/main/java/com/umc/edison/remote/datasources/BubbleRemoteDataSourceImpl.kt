package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.model.bubble.PositionBubbleEntity
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
import com.umc.edison.data.model.bubble.SyncBubbleEntity
import com.umc.edison.remote.api.BubbleApiService
import com.umc.edison.remote.api.BubbleSpaceApiService
import com.umc.edison.remote.api.SyncApiService
import com.umc.edison.remote.model.bubble.toData
import com.umc.edison.remote.model.sync.toSyncBubbleRequest
import javax.inject.Inject

class BubbleRemoteDataSourceImpl @Inject constructor(
    private val bubbleApiService: BubbleApiService,
    private val bubbleSpaceApiService: BubbleSpaceApiService,
    private val syncApiService: SyncApiService,
) : BubbleRemoteDataSource {
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
    override suspend fun syncBubble(bubble: BubbleEntity): SyncBubbleEntity {
        return syncApiService.syncBubble(bubble.toSyncBubbleRequest()).data.toData()
    }
}
