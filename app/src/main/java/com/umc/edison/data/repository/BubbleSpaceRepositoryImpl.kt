package com.umc.edison.data.repository

import com.umc.edison.data.bound.flowDataResource
import com.umc.edison.data.datasources.BubbleSpaceLocalDataSource
import com.umc.edison.data.datasources.BubbleSpaceRemoteDataSource
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.Bubble
import com.umc.edison.domain.repository.BubbleSpaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BubbleSpaceRepositoryImpl @Inject constructor(
    private val bubbleSpaceRemoteDataSource: BubbleSpaceRemoteDataSource,
    private val bubbleSpaceLocalDataSource: BubbleSpaceLocalDataSource
) : BubbleSpaceRepository {

    override fun getAllBubbles(): Flow<DataResource<List<Bubble>>> = flowDataResource(
        localDataAction = { bubbleSpaceLocalDataSource.getAllBubbles() },
        getNotSyncedAction = { bubbleSpaceLocalDataSource.getNotSyncedBubbles() },
        syncRemoteAction = { bubbles -> bubbleSpaceRemoteDataSource.syncBubbles(bubbles as List<BubbleEntity>) }
    )
}