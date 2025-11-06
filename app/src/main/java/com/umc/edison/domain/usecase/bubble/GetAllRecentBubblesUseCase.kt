package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.bubble.Bubble
import com.umc.edison.domain.repository.BubbleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRecentBubblesUseCase @Inject constructor(
    private val bubbleRepository: BubbleRepository
) {
    private val dayBefore = 30
    operator fun invoke(): Flow<DataResource<List<Bubble>>> =
        bubbleRepository.getAllRecentBubbles(dayBefore)
}