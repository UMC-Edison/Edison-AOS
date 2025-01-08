package com.umc.edison.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.room.RoomConstant

@Dao
interface LabelDao : BaseDao<LabelLocal> {
    @Query(
        "SELECT * FROM ${RoomConstant.Table.LABEL} " +
                "Where id IN (" +
                "SELECT labelId FROM ${RoomConstant.Table.BUBBLE_LABEL} WHERE bubbleId = :bubbleId" +
                ")"
    )
    fun getAllLabelsByBubbleId(bubbleId: Int): List<LabelLocal>
}