package com.umc.edison.presentation.model

import com.umc.edison.domain.model.bubble.Bubble
import com.umc.edison.presentation.edison.parseHtml
import java.util.Date
import java.util.UUID
import java.util.LinkedList

data class BubbleModel(
    val id: String?,
    val title: String?,
    val contentBlocks: LinkedList<ContentBlockModel>, // LinkedList로 변경
    val mainImage: String?,
    val labels: List<LabelModel>,
    val backLinks: List<BubbleModel>,
    val linkedBubble: BubbleModel?,
    val date: Date
) {
    fun toDomain(): Bubble {
        return Bubble(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            content = contentBlocks,
            mainImage = mainImage,
            labels = labels.map { it.toDomain() },
            backLinks = backLinks.map { it.toDomain() },
            linkedBubble = linkedBubble?.toDomain(),
            date = date
        )
    }

    companion object {
        val DEFAULT = BubbleModel(
            id = null,
            title = null,
            contentBlocks = LinkedList(), // 빈 LinkedList로 초기화
            mainImage = null,
            labels = emptyList(),
            backLinks = emptyList(),
            linkedBubble = null,
            date = Date()
        )
    }
}

fun Bubble.toPresentation(): BubbleModel {
    val contentBlocks = content?.let { contentString ->
        // contentString을 직접 처리하여 ContentBlockModel 리스트로 변환
        contentString.split("%<").mapIndexed { idx, s ->
            val type = when {
                s.startsWith("${ContentType.TEXT}>") -> ContentType.TEXT
                s.startsWith("${ContentType.IMAGE}>") -> ContentType.IMAGE
                else -> return@mapIndexed null
            }
            val content = when (type) {
                ContentType.TEXT -> s.substringAfter("${ContentType.TEXT}>")
                    .substringBefore("</${ContentType.TEXT}>")
                ContentType.IMAGE -> s.substringAfter("${ContentType.IMAGE}>")
                    .substringBefore("</${ContentType.IMAGE}>")
            }
            ContentBlockModel(type, content, idx)
        }
    }?.filterNotNull()?.toCollection(LinkedList()) ?: LinkedList() // LinkedList로 변경

    return BubbleModel(
        id,
        title,
        contentBlocks,  // LinkedList<ContentBlockModel>
        mainImage?.ifEmpty { null },
        labels.toPresentation(),
        backLinks.toPresentation(),
        linkedBubble?.toPresentation(),
        date
    )
}

fun List<Bubble>.toPresentation(): List<BubbleModel> = map { it.toPresentation() }

fun BubbleModel.getDisplayTitle(): String {
    val selectedTitle = this.title?.takeIf { it.isNotBlank() }
        ?: this.contentBlocks
            .filter { it.type == ContentType.TEXT }
            .firstOrNull { it.content.parseHtml().isNotBlank() }
            ?.content
            ?.parseHtml()
            ?.take(20)
        ?: "제목 없음"

    return selectedTitle.split("\n").first()
}
