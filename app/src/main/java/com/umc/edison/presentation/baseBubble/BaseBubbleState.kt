package com.umc.edison.presentation.baseBubble

import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentBlockModel
import java.util.LinkedList

abstract class BaseBubbleState<M : BaseBubbleMode>(
    open val selectedBubbles: List<BubbleModel>,
    open val mode: BaseBubbleMode,
    open val contentBlocks: LinkedList<ContentBlockModel> // LinkedList<ContentBlockModel> 추가
) {
    abstract fun copyState(
        selectedBubbles: List<BubbleModel> = this.selectedBubbles,
        mode: BaseBubbleMode = this.mode,
        contentBlocks: LinkedList<ContentBlockModel> = this.contentBlocks // contentBlocks 추가
    ): BaseBubbleState<M>
}
