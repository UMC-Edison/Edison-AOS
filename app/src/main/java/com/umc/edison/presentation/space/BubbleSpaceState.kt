package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.BubbleModel

data class BubbleSpaceState(
    val tabs: List<String>,
    val selectedTabIndex: Int,
    val mode: BubbleSpaceMode,
    val selectedBubble: BubbleModel?,
    val query: String,
    val searchResults: List<BubbleModel>,
    val isLoggedIn: Boolean,
    val keywordForMap: String?
) {
    companion object {
        val DEFAULT = BubbleSpaceState(
            tabs = listOf("스페이스", "라벨"),
            selectedTabIndex = 0,
            mode = BubbleSpaceMode.DEFAULT,
            selectedBubble = null,
            query = "",
            searchResults = emptyList(),
            isLoggedIn = false,
            keywordForMap = null
        )
    }
}

enum class BubbleSpaceMode {
    DEFAULT,
    SEARCH,
}
