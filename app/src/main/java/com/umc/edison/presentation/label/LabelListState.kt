package com.umc.edison.presentation.label

import com.umc.edison.presentation.model.LabelModel

data class LabelListState(
    val labels: List<LabelModel>,
    val bubbleCount: Int,
    val selectedLabel: LabelModel,
    val labelEditMode: LabelEditMode,
) {
    companion object {
        val DEFAULT = LabelListState(
            labels = emptyList(),
            bubbleCount = 0,
            selectedLabel = LabelModel.INIT,
            labelEditMode = LabelEditMode.NONE,
        )
    }
}
