package com.umc.edison.local.datasources

import com.umc.edison.data.datasources.BubbleSpaceLocalDataSource
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.local.model.toData
import com.umc.edison.local.room.dao.BubbleDao
import com.umc.edison.local.room.dao.LabelDao
import javax.inject.Inject

class BubbleSpaceLocalDataSourceImpl @Inject constructor(
    private val bubbleDao: BubbleDao,
    private val labelDao: LabelDao
) : BubbleSpaceLocalDataSource {
    override suspend fun getAllBubbles(): List<BubbleEntity> {
        val bubbles = bubbleDao.getAllBubbles().toData()

        bubbles.map { bubble ->
            bubble.labels = labelDao.getAllLabelsByBubbleId(bubble.id).toData()
        }

        return bubbles
    }

    override suspend fun getNotSyncedBubbles(): List<BubbleEntity> {
        val bubbles = bubbleDao.getNotSyncedBubbles().toData()

        bubbles.map { bubble ->
            bubble.labels = labelDao.getAllLabelsByBubbleId(bubble.id).toData()
        }

        return bubbles
    }
}