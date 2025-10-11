package com.umc.edison.presentation.label

import com.umc.edison.presentation.model.LabelModel

data class LabelListState(
    val labels: List<LabelModel>,
    val selectedLabel: LabelModel,
    val bubbleCount: Int,
    val labelEditMode: LabelEditMode,
) {
    companion object {
        val DEFAULT = LabelListState(
            labels = emptyList(),
            selectedLabel = LabelModel.INIT,
            bubbleCount = 0,
            labelEditMode = LabelEditMode.NONE,
        )
    }
}
