package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.BubbleSpaceRemoteDataSource
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.remote.api.BubbleSpaceApiService
import javax.inject.Inject

class BubbleSpaceRemoteDataSourceImpl @Inject constructor(
    private val apiService: BubbleSpaceApiService
) : BubbleSpaceRemoteDataSource {
    override suspend fun getAllBubbles(): List<BubbleEntity> {
        return apiService.getAllBubbles().data.map { it.toData() }
    }
}