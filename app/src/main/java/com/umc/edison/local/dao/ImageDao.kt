package com.umc.edison.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.ImageLocal

@Dao
interface ImageDao {
    @Query("SELECT * FROM ImageLocal Where id IN (SELECT imageId FROM BubbleImageLocal WHERE bubbleId = :bubbleId)")
    fun getAllImagesByBubbleId(bubbleId: Int): List<ImageLocal>

    @Query("SELECT * FROM ImageLocal Where id IN (SELECT imageId FROM BubbleImageLocal WHERE bubbleId = :bubbleId AND isMainImage = 1)")
    fun getMainImageByBubbleId(bubbleId: Int): ImageLocal?
}