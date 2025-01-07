package com.umc.edison.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.umc.edison.local.dao.BubbleDao
import com.umc.edison.local.model.BubbleImageLocal
import com.umc.edison.local.model.BubbleLabelLocal
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.ImageLocal
import com.umc.edison.local.model.LabelLocal

@Database(entities = [BubbleLocal::class, LabelLocal::class, ImageLocal::class, BubbleLabelLocal::class, BubbleImageLocal::class], version = 1)
abstract class EdisonDatabase : RoomDatabase() {
    abstract fun bubbleDao(): BubbleDao

    companion object {
        private const val DATABASE_NAME = "edison_database"
        private var instance: EdisonDatabase? = null

        @Synchronized
        fun getInstance(context: Context): EdisonDatabase? {
            if (instance == null) {
                instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    EdisonDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
            return instance
        }
    }
}