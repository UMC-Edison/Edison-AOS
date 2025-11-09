package com.umc.edison.presentation.model

import com.umc.edison.domain.model.bubble.KeywordBubble

data class KeywordBubbleModel(
    val bubble: BubbleModel,
    val similarity: Float
)

fun KeywordBubble.toPresentation(): KeywordBubbleModel {
    return KeywordBubbleModel(
        bubble = bubble.toPresentation(),
        similarity = similarity
    )
}

fun List<KeywordBubble>.toPresentation(): List<KeywordBubbleModel> {
    return map { it.toPresentation() }
}
