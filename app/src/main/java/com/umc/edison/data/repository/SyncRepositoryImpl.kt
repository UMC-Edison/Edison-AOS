package com.umc.edison.data.repository

import android.util.Log
import com.umc.edison.data.datasources.BubbleLocalDataSource
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.datasources.LabelLocalDataSource
import com.umc.edison.data.datasources.LabelRemoteDataSource
import com.umc.edison.domain.repository.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val bubbleRemoteDataSource: BubbleRemoteDataSource,
    private val labelRemoteDataSource: LabelRemoteDataSource,
    private val bubbleLocalDataSource: BubbleLocalDataSource,
     private val labelLocalDataSource: LabelLocalDataSource,
) : SyncRepository {
    override suspend fun syncLocalDataToServer() {
        Log.i("syncLocalDataToServer", "syncLocalDataToServer is started")

        val unSyncedLabels = labelLocalDataSource.getUnSyncedLabels()
        unSyncedLabels.forEach { label ->
            val syncedLabel = labelRemoteDataSource.syncLabel(label)
            if (syncedLabel.same(label)) {
                labelLocalDataSource.markAsSynced(syncedLabel)
            } else {
                Log.e("syncLocalDataToServer", "Failed to sync label: ${label.id}")
            }
        }

        val unSyncedBubbles = bubbleLocalDataSource.getUnSyncedBubbles()
        unSyncedBubbles.forEach { bubble ->
            val syncedBubble = bubbleRemoteDataSource.syncBubble(bubble)
            if (syncedBubble.same(bubble)) {
                bubbleLocalDataSource.markAsSynced(bubble)
            } else {
                Log.e("syncLocalDataToServer", "Failed to sync bubble: ${bubble.id}")
            }
        }
    }

    override suspend fun syncServerDataToLocal() {
        Log.i("syncServerDataToLocal", "syncServerDataToLocal is started")
        val remoteLabels = labelRemoteDataSource.getAllLabels()
        labelLocalDataSource.syncLabels(remoteLabels)

        val remoteBubbles = bubbleRemoteDataSource.getAllBubbles()
        bubbleLocalDataSource.syncBubbles(remoteBubbles)
    }
}