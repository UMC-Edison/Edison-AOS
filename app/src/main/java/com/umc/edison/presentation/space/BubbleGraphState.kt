package com.umc.edison.presentation.space

import com.umc.edison.presentation.model.ClusterModel
import com.umc.edison.presentation.model.EdgeDataModel
import com.umc.edison.presentation.model.PositionedBubbleModel

data class BubbleGraphState(
    val clusters: List<ClusterModel>,
    val bubbles: List<PositionedBubbleModel>,
    val edges: List<EdgeDataModel>,
) {
    companion object {
        val DEFAULT = BubbleGraphState(
            clusters = emptyList(),
            bubbles = emptyList(),
            edges = emptyList(),
        )

        const val BUBBLE_DOT_RADIUS = 12f
    }
}
