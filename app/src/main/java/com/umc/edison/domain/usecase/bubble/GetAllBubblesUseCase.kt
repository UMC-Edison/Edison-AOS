package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.Bubble
import com.umc.edison.domain.repository.BubbleSpaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllBubblesUseCase @Inject constructor(
    private val bubbleSpaceRepository: BubbleSpaceRepository
) {
    operator fun invoke(): Flow<DataResource<List<Bubble>>> = bubbleSpaceRepository.getAllBubbles()
}