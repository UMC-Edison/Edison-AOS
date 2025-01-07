package com.umc.edison.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.umc.edison.local.model.BubbleLocal

@Dao
interface BubbleDao {
    @Insert
    fun insert(bubble: BubbleLocal)
}