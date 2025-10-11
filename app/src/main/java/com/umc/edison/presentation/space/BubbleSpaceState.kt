package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.BubbleModel

data class BubbleSpaceState(
    val mode: BubbleSpaceMode,
    val selectedBubble: BubbleModel?,
    val query: String,
    val searchResults: List<BubbleModel>,
    val isLoggedIn: Boolean,
    val keywordForMap: String?
) {
    companion object {
        val DEFAULT = BubbleSpaceState(
            mode = BubbleSpaceMode.GRAPH,
            selectedBubble = null,
            query = "",
            searchResults = emptyList(),
            isLoggedIn = false,
            keywordForMap = null
        )
    }
}

enum class BubbleSpaceMode {
    GRAPH,
    SEARCH,
    KEYWORD
}
