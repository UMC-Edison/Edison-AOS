package com.umc.edison.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.LabelLocal

@Dao
interface LabelDao {
    @Query("SELECT * FROM LabelLocal Where id IN (SELECT labelId FROM BubbleLabelLocal WHERE bubbleId = :bubbleId)")
    fun getAllLabelsByBubbleId(bubbleId: Int): List<LabelLocal>
}