package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.BubbleSpaceRemoteDataSource
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.remote.api.BubbleSpaceApiService
import com.umc.edison.remote.model.toRemote
import javax.inject.Inject

class BubbleSpaceRemoteDataSourceImpl @Inject constructor(
    private val apiService: BubbleSpaceApiService
) : BubbleSpaceRemoteDataSource {
    override suspend fun getAllBubbles(): List<BubbleEntity> = apiService.getAllBubbles().data.map { it.toData() }

    override suspend fun syncBubbles(bubbles: List<BubbleEntity>) {
        apiService.syncBubbles(bubbles.map { it.toRemote() })
    }
}