package com.umc.edison.local.di

import android.content.Context
import androidx.room.Room
import com.umc.edison.local.room.EdisonDatabase
import com.umc.edison.local.room.RoomConstant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalRoomModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): EdisonDatabase = Room.databaseBuilder(
        context,
        EdisonDatabase::class.java,
        RoomConstant.ROOM_DB_NAME
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideBubbleDao(database: EdisonDatabase) = database.bubbleDao()

    @Provides
    @Singleton
    fun provideLabelDao(database: EdisonDatabase) = database.labelDao()
}