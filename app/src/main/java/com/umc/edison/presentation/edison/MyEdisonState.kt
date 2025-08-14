package com.umc.edison.presentation.edison

import com.umc.edison.presentation.model.BubbleModel

data class MyEdisonState(
    val bubbles: List<BubbleModel>,
    val query: String,
    val searchResults: List<BubbleModel>,
) {
    companion object {
        val DEFAULT = MyEdisonState(
            bubbles = emptyList(),
            query = "",
            searchResults = emptyList(),
        )
    }
}
