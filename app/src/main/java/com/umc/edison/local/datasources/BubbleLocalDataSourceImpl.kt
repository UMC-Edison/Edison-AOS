package com.umc.edison.local.datasources

import android.icu.util.Calendar
import com.umc.edison.data.datasources.BubbleLocalDataSource
import com.umc.edison.data.model.bubble.BubbleEntity
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.model.toLocal
import com.umc.edison.local.room.RoomConstant
import com.umc.edison.local.room.dao.BubbleDao
import com.umc.edison.local.room.dao.BubbleLabelDao
import com.umc.edison.local.room.dao.LabelDao
import com.umc.edison.local.room.dao.LinkedBubbleDao
import javax.inject.Inject

class BubbleLocalDataSourceImpl @Inject constructor(
    private val bubbleDao: BubbleDao,
    private val labelDao: LabelDao,
    private val bubbleLabelDao: BubbleLabelDao,
    private val linkedBubbleDao: LinkedBubbleDao
) : BubbleLocalDataSource, BaseLocalDataSourceImpl<BubbleLocal>(bubbleDao) {

    private val tableName = RoomConstant.getTableNameByClass(BubbleLocal::class.java)

    // CREATE
    override suspend fun addBubbles(bubbles: List<BubbleEntity>) {
        bubbles.map { bubble ->
            addBubble(bubble)
        }
    }

    override suspend fun addBubble(bubble: BubbleEntity): BubbleEntity {
        val id = insert(bubble.toLocal())
        val insertedBubble = bubble.copy(id = id)

        addBubbleLabel(insertedBubble)
        addLinkedBubble(insertedBubble)

        return getActiveBubble(insertedBubble.id)
    }

    // READ
    override suspend fun getAllActiveBubbles(): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllActiveBubbles()

        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllRecentBubbles(dayBefore: Int): List<BubbleEntity> {
        val dayBefore = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dayBefore)
        }.time.time
        val localBubbles: List<BubbleLocal> = bubbleDao.getAllRecentBubbles(dayBefore)

        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getAllTrashedBubbles(): List<BubbleEntity> {
        val deletedBubbles: List<BubbleLocal> = bubbleDao.getAllTrashedBubbles()

        return convertLocalBubblesToBubbleEntities(deletedBubbles)
    }

    override suspend fun getActiveBubble(id: String): BubbleEntity {
        val bubble = bubbleDao.getActiveBubbleById(id)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found")

        val result = bubble.copy(
            labels = labelDao.getAllActiveLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getActiveBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getActiveLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getRawBubble(id: String): BubbleEntity {
        val bubble = bubbleDao.getRawBubbleById(id)?.toData()
            ?: throw IllegalArgumentException("Bubble with id $id not found")

        val result = bubble.copy(
            labels = labelDao.getAllRawLabelsByBubbleId(id).map { it.toData() },
            backLinks = linkedBubbleDao.getRawBackLinksByBubbleId(id).map { it.toData() },
            linkedBubble = linkedBubbleDao.getRawLinkedBubbleByBubbleId(id)?.toData()
        )

        return result
    }

    override suspend fun getBubblesByLabelId(labelId: String): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesByLabelId(labelId)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getBubblesWithoutLabel(): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = bubbleDao.getBubblesWithoutLabel()
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getSearchBubbleResults(query: String): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = bubbleDao.getSearchBubbles(query)
        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    override suspend fun getUnSyncedBubbles(): List<BubbleEntity> {
        val localBubbles: List<BubbleLocal> = getAllUnSyncedRows(tableName)

        return convertLocalBubblesToBubbleEntities(localBubbles)
    }

    // UPDATE
    override suspend fun updateBubbles(bubbles: List<BubbleEntity>) {
        bubbles.map { bubble ->
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

        return getActiveBubble(bubble.id)
    }

    override suspend fun markAsSynced(bubble: BubbleEntity) {
        markAsSynced(tableName, bubble.id)
    }

    override suspend fun syncBubbles(bubbles: List<BubbleEntity>) {
        bubbles.map {
            syncBubble(it)
        }
    }

    private suspend fun syncBubble(bubble: BubbleEntity) {
        // bubble이 갖고있는 backLinks와 linkedBubble 정보 먼저 업데이트
        for(backLink in bubble.backLinks) {
            try {
                val savedBubble = getActiveBubble(backLink.id)
                if (savedBubble.same(backLink) && savedBubble.updatedAt > backLink.updatedAt) continue
                updateBubble(backLink, true)
            } catch (_: IllegalArgumentException) {
                addBubble(backLink)
            }
            markAsSynced(backLink)
        }

        // linkedBubble 정보 업데이트
        try {
            bubble.linkedBubble?.let { linkedBubble ->
                val savedBubble = getActiveBubble(linkedBubble.id)
                if (savedBubble.same(linkedBubble)) return@let
                updateBubble(linkedBubble)
            }
        } catch (_: IllegalArgumentException) {
            bubble.linkedBubble?.let { addBubble(it) }
        }
        bubble.linkedBubble?.let { markAsSynced(it) }

        // 현재 버블 업데이트
        try {
            val savedBubble = getActiveBubble(bubble.id)
            if (savedBubble.same(bubble)) return
            updateBubble(bubble)
        } catch (_: IllegalArgumentException) {
            addBubble(bubble)
        }
        markAsSynced(bubble)
    }

    // DELETE
    override suspend fun deleteBubbles(bubbles: List<BubbleEntity>) {
        bubbleDao.deleteBubbles(bubbles.map { it.id })
    }

    // Helper function
    private suspend fun addBubbleLabel(bubble: BubbleEntity) {
        bubble.labels.map { label ->
            val localLabel: LabelLocal? = labelDao.getLabelById(label.id)
            if (localLabel == null) {
                labelDao.insert(label.toLocal())
                bubbleLabelDao.insert(bubble.id, label.id)
            } else {
                val id = bubbleLabelDao.getBubbleLabelId(bubble.id, localLabel.uuid)
                if (id == null) bubbleLabelDao.insert(bubble.id, localLabel.uuid)
            }
        }
    }

    private suspend fun addLinkedBubble(bubble: BubbleEntity) {
        bubble.linkedBubble?.let { linkedBubble ->
            val id = linkedBubbleDao.getLinkedBubbleId(bubble.id, linkedBubble.id, false)
            if (id == null) linkedBubbleDao.insert(bubble.id, linkedBubble.id, false)
        }

        bubble.backLinks.map { backLink ->
            bubbleDao.getActiveBubbleById(backLink.id) ?: return@map
            val id = linkedBubbleDao.getLinkedBubbleId(bubble.id, backLink.id, true)
            if (id == null) linkedBubbleDao.insert(bubble.id, backLink.id, true)
        }
    }

    private suspend fun convertLocalBubblesToBubbleEntities(localBubbles: List<BubbleLocal>): List<BubbleEntity> {
        val bubbles: MutableList<BubbleEntity> = mutableListOf()

        localBubbles.map {
            bubbles += getActiveBubble(it.uuid)
        }

        return bubbles
    }
}