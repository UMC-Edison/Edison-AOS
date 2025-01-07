package com.umc.edison.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.umc.edison.local.model.BubbleLocal

@Dao
interface BubbleDao {
    @Insert
    fun insert(bubble: BubbleLocal)

    @Query("SELECT * FROM BubbleLocal")
    fun getAllBubbles(): List<BubbleLocal>
}