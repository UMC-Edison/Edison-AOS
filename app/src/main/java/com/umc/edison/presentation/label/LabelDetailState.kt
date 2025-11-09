package com.umc.edison.presentation.label

import com.umc.edison.presentation.baseBubble.BaseBubbleMode
import com.umc.edison.presentation.baseBubble.BaseBubbleState
import com.umc.edison.presentation.baseBubble.LabelDetailMode
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.LabelModel

data class LabelDetailState(
    val label: LabelModel,
    val bubbles: List<BubbleModel>,
    val movableLabels: List<LabelModel>,
    override val selectedBubbles: List<BubbleModel>,
    override val mode: BaseBubbleMode,
) : BaseBubbleState<LabelDetailMode>(selectedBubbles, mode) {
    companion object {
        val DEFAULT = LabelDetailState(
            label = LabelModel.INIT,
            bubbles = emptyList(),
            movableLabels = emptyList(),
            selectedBubbles = emptyList(),
            mode = LabelDetailMode.NONE,
        )
    }

    override fun copyState(
        selectedBubbles: List<BubbleModel>,
        mode: BaseBubbleMode,
    ): BaseBubbleState<LabelDetailMode> {
        return copy(
            selectedBubbles = selectedBubbles,
            mode = mode,
        )
    }
}
