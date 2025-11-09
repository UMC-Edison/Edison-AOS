package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.bubble.KeywordBubble
import com.umc.edison.domain.repository.BubbleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetKeywordBubbleUsecase @Inject constructor(
    private val bubbleRepository: BubbleRepository
) {
    operator fun invoke(keyword: String): Flow<DataResource<List<KeywordBubble>>> =
        bubbleRepository.getKeywordBubbles(keyword)
}