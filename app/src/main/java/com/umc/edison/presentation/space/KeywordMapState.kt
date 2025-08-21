package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.KeywordBubbleModel

data class KeywordMapState(
    val bubbles: List<KeywordBubbleModel>,
) {
    companion object {
        val DEFAULT = KeywordMapState(
            bubbles = emptyList(),
        )
    }
}
