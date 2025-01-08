package com.umc.edison.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.umc.edison.local.model.BubbleLabelLocal
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.room.dao.BubbleDao
import com.umc.edison.local.room.dao.LabelDao

@Database(
    entities = [BubbleLocal::class, LabelLocal::class, BubbleLabelLocal::class],
    version = RoomConstant.ROOM_VERSION
)
@TypeConverters(DtoConverter::class)
abstract class EdisonDatabase : RoomDatabase() {
    abstract fun bubbleDao(): BubbleDao
    abstract fun labelDao(): LabelDao
}