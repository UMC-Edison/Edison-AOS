package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.BubbleModel

data class BubbleSpaceState(
    val isLoading: Boolean,
    val bubbles: List<BubbleModel>,
    val error: Throwable?,
) {
    companion object {
        val DEFAULT = BubbleSpaceState(
            isLoading = false,
            bubbles = emptyList(),
            error = null
        )
    }
}