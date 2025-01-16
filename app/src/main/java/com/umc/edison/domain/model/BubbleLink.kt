package com.umc.edison.domain.model

import com.umc.edison.local.model.BubbleLocal

data class BubbleLink(
    val source: BubbleLocal, // 작성 중인 버블 (버블1)
    val target: BubbleLocal  // 선택된 버블 (버블2)
)
