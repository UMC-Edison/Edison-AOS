package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.Bubble
import kotlinx.coroutines.flow.Flow

interface BubbleSpaceRepository {
    fun getAllBubbles(): Flow<DataResource<List<Bubble>>>
}