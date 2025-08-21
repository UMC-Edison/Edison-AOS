package com.umc.edison.data.model.bubble


import com.umc.edison.data.model.DataMapper
import com.umc.edison.domain.model.bubble.KeywordBubble

data class KeywordBubbleEntity (
    val bubble : BubbleEntity,
    val similarity : Float,
): DataMapper<KeywordBubble>{
    override fun toDomain(): KeywordBubble=KeywordBubble(
        bubble = bubble.toDomain(),
        similarity = similarity
    )
}
