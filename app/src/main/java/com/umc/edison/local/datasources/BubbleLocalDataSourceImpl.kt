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

    override suspend fun linkGuestBubblesToUser(userId: String) {
        bubbleDao.linkGuestBubblesToUser(userId)
    }

    // --- CREATE ---
    override suspend fun addBubbles(bubbles: List<BubbleEntity>) {
        bubbles.forEach { bubble ->
            addBubble(bubble, null)
        }
    }

    override suspend fun addBubble(bubble: BubbleEntity, userId: String?): BubbleEntity {
        val targetUserId = userId ?: tokenManager.getUserId()
        val localBubble = BubbleLocal(
            uuid = bubble.id,
            userId = targetUserId,
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
        addLinkedBubble(insertedBubble)
        return getActiveBubble(insertedBubble.id)
    }

    // --- READ ---
    override suspend fun getAllActiveBubbles(): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllActiveBubbles(userId)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllRecentBubbles(dayBefore: Int): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val timestampLimit = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dayBefore)
        }.time.time
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllRecentBubbles(timestampLimit, userId)

        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllTrashedBubbles(): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val deletedBubbles: List<BubbleLocal> = bubbleDao.getAllTrashedBubbles(userId)

        return convertLocalBubblesToBubbleEntities(deletedBubbles)
    }

    override suspend fun getActiveBubble(id: String): BubbleEntity {
        val userId = tokenManager.getUserId()
        val bubble = bubbleDao.getActiveBubbleById(id, userId)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found for current user")

        val result = bubble.copy(
            labels = labelDao.getAllActiveLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getActiveBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getActiveLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getRawBubble(id: String): BubbleEntity {
        val userId = tokenManager.getUserId()
        val bubble = bubbleDao.getRawBubbleById(id, userId)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found")

        val result = bubble.copy(
            labels = labelDao.getAllRawLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getRawBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getRawLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getBubblesByLabelId(labelId: String): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesByLabelId(labelId, userId)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getBubblesWithoutLabel(): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesWithoutLabel(userId)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getSearchBubbleResults(query: String): List<BubbleEntity> {
        val userId = tokenManager.getUserId()
        val localBubbles: List<BubbleLocal> = bubbleDao.getSearchBubbles(query, userId)
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

        // 배치로 처리하기 위해 모든 관련 버블 ID 수집
        val allBubbleIds = mutableSetOf<String>()
        bubbles.forEach { bubble ->
            allBubbleIds.add(bubble.id)
            bubble.backLinks.forEach { allBubbleIds.add(it.id) }
            bubble.linkedBubble?.let { allBubbleIds.add(it.id) }
        }

        // 기존 버블들을 배치로 조회
        val userId = tokenManager.getUserId()
        val existingBubbles = convertLocalBubblesToBubbleEntities(
            bubbleDao.getActiveBubblesByIds(allBubbleIds.toList(), userId)
        ).associateBy { it.id }

        // 각 버블 동기화
        bubbles.forEach { bubble ->
            syncBubbleWithExistingData(bubble, existingBubbles)
        }
    }

    private suspend fun syncBubbleWithExistingData(
        bubble: BubbleEntity,
        existingBubbles: Map<String, BubbleEntity>
    ) {
        // backLinks 처리
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

        // linkedBubble 처리
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

        // 현재 버블 처리
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

        // 존재하지 않는 라벨들을 배치로 삽입
        val newLabels = bubble.labels.filter { it.id !in existingLabelIds }
        newLabels.forEach { label ->
            labelDao.insert(label.toLocal())
        }

        // 버블-라벨 관계 확인 및 삽입
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
        // LinkedBubble 처리
        bubble.linkedBubble?.let { linkedBubble ->
            val id = linkedBubbleDao.getLinkedBubbleId(bubble.id, linkedBubble.id, false)
            if (id == null) linkedBubbleDao.insert(bubble.id, linkedBubble.id, false)
        }

        // 버블-라벨 관계 확인 및 삽입
        if (bubble.backLinks.isNotEmpty()) {
            val backLinkIds = bubble.backLinks.map { it.id }
            val userId = tokenManager.getUserId()
            val existingBubbles = bubbleDao.getActiveBubblesByIds(backLinkIds, userId)
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

        // 배치로 모든 관련 데이터를 한 번에 조회
        val labelsWithBubbleId = labelDao.getAllActiveLabelsByBubbleIds(bubbleIds)
        val linkedBubblesWithParentId = linkedBubbleDao.getActiveLinkedBubblesByBubbleIds(bubbleIds)
        val backLinksWithParentId = linkedBubbleDao.getActiveBackLinksByBubbleIds(bubbleIds)

        // 버블 ID별로 그룹화
        val labelsByBubbleId = labelsWithBubbleId.groupBy { it.bubbleId }
        val linkedBubblesByBubbleId = linkedBubblesWithParentId.groupBy { it.parentBubbleId }
        val backLinksByBubbleId = backLinksWithParentId.groupBy { it.parentBubbleId }

        // 각 버블에 대해 관련 데이터를 조합하여 BubbleEntity 생성
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