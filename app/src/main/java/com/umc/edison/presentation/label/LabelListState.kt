package com.umc.edison.presentation.label

import com.umc.edison.presentation.model.LabelModel

data class LabelListState(
    val labels: List<LabelModel>,
    val selectedLabel: LabelModel,
    val labelEditMode: LabelEditMode,
) {
    val bubbleCount: Int
        get() = labels.sumOf { it.bubbleCnt }

    companion object {
        val DEFAULT = LabelListState(
            labels = emptyList(),
            selectedLabel = LabelModel.INIT,
            labelEditMode = LabelEditMode.NONE,
        )
    }
}
