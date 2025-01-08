package com.umc.edison.data.datasources

import com.umc.edison.data.model.BubbleEntity

interface BubbleSpaceLocalDataSource {
    suspend fun getAllBubbles(): List<BubbleEntity>
    suspend fun getNotSyncedBubbles(): List<BubbleEntity>
}