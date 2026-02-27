package com.umc.edison.local.datasources

import android.icu.util.Calendar
import com.umc.edison.data.datasources.BubbleLocalDataSource
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.data.token.TokenManager
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.toLocal
import com.umc.edison.local.room.RoomConstant
import com.umc.edison.local.room.dao.BubbleDao
import com.umc.edison.local.room.dao.BubbleLabelDao
import com.umc.edison.local.room.dao.LabelDao
import com.umc.edison.local.room.dao.LinkedBubbleDao
import java.util.Date
import javax.inject.Inject

class BubbleLocalDataSourceImpl @Inject constructor(
    private val bubbleDao: BubbleDao,
    private val labelDao: LabelDao,
    private val bubbleLabelDao: BubbleLabelDao,
    private val linkedBubbleDao: LinkedBubbleDao,
    private val tokenManager: TokenManager
) : BubbleLocalDataSource, BaseLocalDataSourceImpl<BubbleLocal>(bubbleDao) {

    private val tableName = RoomConstant.getTableNameByClass(BubbleLocal::class.java)

    override suspend fun linkGuestBubblesToUser(userEmail: String) {
        bubbleDao.linkGuestBubblesToUser(userEmail)
    }

    // --- CREATE ---
    override suspend fun addBubbles(bubbles: List<BubbleEntity>) {
        val userEmail = tokenManager.getUserEmail()
        bubbles.forEach { bubble ->
            addBubble(bubble, userEmail)
        }
    }

    override suspend fun addBubble(bubble: BubbleEntity, userEmail: String?): BubbleEntity {
        val targetUserEmail = userEmail ?: tokenManager.getUserEmail()
        val localBubble = BubbleLocal(
            uuid = bubble.id,
            userEmail = targetUserEmail,
            title = bubble.title,
            content = bubble.content,
            mainImage = bubble.mainImage,
            isSynced = bubble.isSynced,
            isTrashed = bubble.isTrashed,
            isDeleted = bubble.isDeleted,
            createdAt = bubble.createdAt,
            updatedAt = bubble.updatedAt,
            deletedAt = bubble.deletedAt
        )

        val id = insert(localBubble)

        val insertedBubble = bubble.copy(id = id)
        addBubbleLabel(insertedBubble)
        return getActiveBubble(insertedBubble.id)
    }

    // --- READ ---
    override suspend fun getAllActiveBubbles(): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllActiveBubbles(userEmail)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllRecentBubbles(dayBefore: Int): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val timestampLimit = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dayBefore)
        }.time.time
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllRecentBubbles(timestampLimit, userEmail)

        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllTrashedBubbles(): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val deletedBubbles: List<BubbleLocal> = bubbleDao.getAllTrashedBubbles(userEmail)

        return convertLocalBubblesToBubbleEntities(deletedBubbles)
    }

    override suspend fun getActiveBubble(id: String): BubbleEntity {
        val userEmail = tokenManager.getUserEmail()
        val bubble = bubbleDao.getActiveBubbleById(id, userEmail)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found for current user")

        val result = bubble.copy(
            labels = labelDao.getAllActiveLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getActiveBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getActiveLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getRawBubble(id: String): BubbleEntity {
        val userEmail = tokenManager.getUserEmail()
        val bubble = bubbleDao.getRawBubbleById(id, userEmail)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found")

        val result = bubble.copy(
            labels = labelDao.getAllRawLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getRawBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getRawLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getBubblesByLabelId(labelId: String): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesByLabelId(labelId, userEmail)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getBubblesWithoutLabel(): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesWithoutLabel(userEmail)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getSearchBubbleResults(query: String): List<BubbleEntity> {
        val userEmail = tokenManager.getUserEmail()
        val localBubbles: List<BubbleLocal> = bubbleDao.getSearchBubbles(query, userEmail)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getUnSyncedBubbles(): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = getAllUnSyncedRows(tableName)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    // --- UPDATE ---
    override suspend fun updateBubbles(bubbles: List<BubbleEntity>) {
        bubbles.forEach { bubble ->
            updateBubble(bubble)
        }
    }

    override suspend fun updateBubble(bubble: BubbleEntity, isSynced: Boolean): BubbleEntity {
        update(bubble.toLocal(), tableName)

        bubbleLabelDao.deleteByBubbleId(bubble.id)
        linkedBubbleDao.deleteLinkedBubble(bubble.id, false)
        linkedBubbleDao.deleteLinkedBubble(bubble.id, true)

        addBubbleLabel(bubble)
        addLinkedBubble(bubble)

        if (isSynced) {
            markAsSynced(bubble)
        }

        return getActiveBubble(bubble.id)
    }

    override suspend fun trashBubbles(bubbles: List<BubbleEntity>) {
        val trashedBubbles = bubbles.map { bubble ->
            bubble.copy(
                isTrashed = true,
                isDeleted = false,
                deletedAt = Date()
            )
        }
        trashedBubbles.forEach {
            update(it.toLocal(), tableName)
        }
    }

    override suspend fun markAsSynced(bubble: BubbleEntity) {
        markAsSynced(tableName, bubble.id)
    }

    override suspend fun syncBubbles(bubbles: List<BubbleEntity>) {
        if (bubbles.isEmpty()) return

        val allBubbleIds = mutableSetOf<String>()
        bubbles.forEach { bubble ->
            allBubbleIds.add(bubble.id)
            bubble.backLinks.forEach { allBubbleIds.add(it.id) }
            bubble.linkedBubble?.let { allBubbleIds.add(it.id) }
        }

        val userEmail = tokenManager.getUserEmail()
        val existingBubbles = convertLocalBubblesToBubbleEntities(
            bubbleDao.getActiveBubblesByIds(allBubbleIds.toList(), userEmail)
        ).associateBy { it.id }

        bubbles.forEach { bubble ->
            syncBubbleWithExistingData(bubble, existingBubbles)
        }
    }

    private suspend fun syncBubbleWithExistingData(
        bubble: BubbleEntity,
        existingBubbles: Map<String, BubbleEntity>
    ) {
        for(backLink in bubble.backLinks) {
            val existingBackLink = existingBubbles[backLink.id]
            if (existingBackLink != null) {
                if (existingBackLink.same(backLink) && existingBackLink.updatedAt > backLink.updatedAt) continue
                updateBubble(backLink, true)
            } else {
                addBubble(backLink, null)
            }
            markAsSynced(backLink)
        }

        bubble.linkedBubble?.let { linkedBubble ->
            val existingLinkedBubble = existingBubbles[linkedBubble.id]
            if (existingLinkedBubble != null) {
                if (!existingLinkedBubble.same(linkedBubble)) {
                    updateBubble(linkedBubble, true)
                }
            } else {
                addBubble(linkedBubble, null)
            }
            markAsSynced(linkedBubble)
        }

        val existingBubble = existingBubbles[bubble.id]
        if (existingBubble != null) {
            if (!existingBubble.same(bubble)) {
                updateBubble(bubble, true)
            }
        } else {
            addBubble(bubble, null)
        }
        markAsSynced(bubble)
    }

    // --- DELETE ---
    override suspend fun deleteBubbles(bubbles: List<BubbleEntity>) {
        bubbleDao.deleteBubbles(bubbles.map { it.id })
    }

    // Helper function
    private suspend fun addBubbleLabel(bubble: BubbleEntity) {
        if (bubble.labels.isEmpty()) return

        val labelIds = bubble.labels.map { it.id }
        val existingLabels = labelDao.getLabelsByIds(labelIds)
        val existingLabelIds = existingLabels.map { it.uuid }.toSet()

        val newLabels = bubble.labels.filter { it.id !in existingLabelIds }
        newLabels.forEach { label ->
            labelDao.insert(label.toLocal())
        }

        val existingRelations = bubbleLabelDao.getBubbleLabelsByIds(listOf(bubble.id), labelIds)
        val existingRelationPairs = existingRelations.map { "${it.bubbleId}-${it.labelId}" }.toSet()

        bubble.labels.forEach { label ->
            val relationKey = "${bubble.id}-${label.id}"
            if (relationKey !in existingRelationPairs) {
                bubbleLabelDao.insert(bubble.id, label.id)
            }
        }
    }

    private suspend fun addLinkedBubble(bubble: BubbleEntity) {
        bubble.linkedBubble?.let { linkedBubble ->
            val id = linkedBubbleDao.getLinkedBubbleId(bubble.id, linkedBubble.id, false)
            if (id == null) linkedBubbleDao.insert(bubble.id, linkedBubble.id, false)
        }

        if (bubble.backLinks.isNotEmpty()) {
            val backLinkIds = bubble.backLinks.map { it.id }
            val userEmail = tokenManager.getUserEmail()
            val existingBubbles = bubbleDao.getActiveBubblesByIds(backLinkIds, userEmail)
            val existingBubbleIds = existingBubbles.map { it.uuid }.toSet()

            bubble.backLinks.forEach { backLink ->
                if (backLink.id in existingBubbleIds) {
                    val id = linkedBubbleDao.getLinkedBubbleId(bubble.id, backLink.id, true)
                    if (id == null) linkedBubbleDao.insert(bubble.id, backLink.id, true)
                }
            }
        }
    }

    private suspend fun convertLocalBubblesToBubbleEntities(localBubbles: List<BubbleLocal>): List<BubbleEntity> {
        if (localBubbles.isEmpty()) return emptyList()

        val bubbleIds = localBubbles.map { it.uuid }

        val labelsWithBubbleId = labelDao.getAllActiveLabelsByBubbleIds(bubbleIds)
        val linkedBubblesWithParentId = linkedBubbleDao.getActiveLinkedBubblesByBubbleIds(bubbleIds)
        val backLinksWithParentId = linkedBubbleDao.getActiveBackLinksByBubbleIds(bubbleIds)

        val labelsByBubbleId = labelsWithBubbleId.groupBy { it.bubbleId }
        val linkedBubblesByBubbleId = linkedBubblesWithParentId.groupBy { it.parentBubbleId }
        val backLinksByBubbleId = backLinksWithParentId.groupBy { it.parentBubbleId }

        return localBubbles.map { localBubble ->
            val bubbleId = localBubble.uuid
            val baseEntity = localBubble.toData()

            baseEntity.copy(
                labels = labelsByBubbleId[bubbleId]?.map { it.toLabelEntity() } ?: emptyList(),
                linkedBubble = linkedBubblesByBubbleId[bubbleId]?.firstOrNull()?.toBubbleEntity(),
                backLinks = backLinksByBubbleId[bubbleId]?.map { it.toBubbleEntity() } ?: emptyList()
            )
        }
    }
}