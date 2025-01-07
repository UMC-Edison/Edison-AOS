package com.umc.edison.data.datasources

import com.umc.edison.data.model.BubbleEntity

interface BubbleSpaceRemoteDataSource {
    suspend fun getAllBubbles(): List<BubbleEntity>
}