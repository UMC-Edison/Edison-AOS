package com.umc.edison.local.datasources

import com.umc.edison.data.datasources.BubbleSpaceLocalDataSource
import com.umc.edison.data.model.BubbleEntity
import com.umc.edison.local.dao.BubbleDao
import com.umc.edison.local.dao.ImageDao
import com.umc.edison.local.dao.LabelDao
import javax.inject.Inject

class BubbleSpaceLocalDataSourceImpl @Inject constructor(
    private val bubbleDao: BubbleDao,
    private val imageDao: ImageDao,
    private val labelDao: LabelDao
) : BubbleSpaceLocalDataSource {
    override suspend fun getAllBubbles(): List<BubbleEntity> {
        val bubbles = bubbleDao.getAllBubbles().map { it.toData() }

        bubbles.map { bubble ->
            bubble.mainImage = imageDao.getMainImageByBubbleId(bubble.id)?.uri
            bubble.images = imageDao.getAllImagesByBubbleId(bubble.id).map { it.uri }
            bubble.labels = labelDao.getAllLabelsByBubbleId(bubble.id).map { it.toData() }
        }

        return bubbles
    }
}