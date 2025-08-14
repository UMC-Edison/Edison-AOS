package com.umc.edison.data.model.bubble

import android.util.Log
import java.util.Date

data class SyncBubbleEntity(
    val id: String,
    val title: String?,
    val content: String?,
    val mainImage: String?,
    val labelIds: List<String>,
    val backLinkIds: List<String>,
    val linkedBubbleId: String?,
    val isDeleted: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val deletedAt: Date? = null,
) {
    fun same(other: BubbleEntity): Boolean {
        Log.d("BubbleEntity", "this: $this,\nother: $other")

        return id == other.id &&
                title == other.title &&
                content == other.content &&
                mainImage == other.mainImage &&
                labelIds == other.labels.map { it.id } &&
                backLinkIds == other.backLinks.map { it.id } &&
                linkedBubbleId == other.linkedBubble?.id &&
                isDeleted == other.isDeleted &&
                isTrashed == other.isTrashed
    }
}
