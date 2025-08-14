package com.umc.edison.presentation.model

import com.umc.edison.domain.model.bubble.Bubble
import java.util.Date
import java.util.UUID

data class BubbleModel(
    val id: String?,
    val title: String?,
    val contentBlocks: List<ContentBlockModel>,
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
            content = contentBlocks.joinToString { it.toDomain() },
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
            contentBlocks = emptyList(),
            mainImage = null,
            labels = emptyList(),
            backLinks = emptyList(),
            linkedBubble = null,
            date = Date()
        )
    }
}

fun Bubble.toPresentation(): BubbleModel {
    // Text 타입의 경우 앞에 %<TEXT>와 뒤에 </TEXT>%가 붙어있고
    // Image 타입의 경우 앞에 %<IMAGE>와 뒤에 </IMAGE>%가 붙어있음
    val contentBlocks = content?.split("%<")?.mapIndexed { idx, s ->
        val type = when {
            s.startsWith("${ContentType.TEXT}>") -> ContentType.TEXT
            s.startsWith("${ContentType.IMAGE}>") -> ContentType.IMAGE
            else -> return@mapIndexed null
        }
        when (type) {
            ContentType.TEXT -> {
                val text = s.substringAfter("${ContentType.TEXT}>")
                    .substringBefore("</${ContentType.TEXT}>")
                ContentBlockModel(type, text, idx)
            }
            ContentType.IMAGE -> {
                val raw = s.substringAfter("${ContentType.IMAGE}>")
                    .substringBefore("</${ContentType.IMAGE}>")
                val (url, key) = raw.split('|').let {
                    when (it.size) {
                        2 -> it[0] to it[1]
                        else -> raw to null
                    }
                }
                ContentBlockModel(
                    type = ContentType.IMAGE,
                    content = url,
                    position = idx,
                    imageKey = key
                )
            }

        }
    }?.filterNotNull() ?: emptyList()

    return BubbleModel(
        id,
        title,
        contentBlocks,
        mainImage?.ifEmpty { null },
        labels.toPresentation(),
        backLinks.toPresentation(),
        linkedBubble?.toPresentation(),
        date
    )
}

fun List<Bubble>.toPresentation(): List<BubbleModel> = map { it.toPresentation() }
