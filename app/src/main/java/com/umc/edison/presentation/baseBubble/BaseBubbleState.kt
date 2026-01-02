package com.umc.edison.presentation.baseBubble

import com.umc.edison.presentation.model.BubbleModel

abstract class BaseBubbleState<M: BaseBubbleMode> (
    open val selectedBubbles: List<BubbleModel>,
    open val mode: BaseBubbleMode,
) {
    abstract fun copyState(
        selectedBubbles: List<BubbleModel> = this.selectedBubbles,
        mode: BaseBubbleMode = this.mode,
    ): BaseBubbleState<M>
}

