package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.KeywordBubbleModel

data class KeywordMapState(
    val bubbles: List<KeywordBubbleModel>,
    val selectedBubble: KeywordBubbleModel?,
    val isBubbleDoorVisible: Boolean

) {
    companion object {
        val DEFAULT = KeywordMapState(
            bubbles = emptyList(),
            selectedBubble = null,
            isBubbleDoorVisible = false
        )
    }
}
