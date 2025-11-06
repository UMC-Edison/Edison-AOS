package com.umc.edison.data.repository

import android.util.Log
import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.BubbleLocalDataSource
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.model.bubble.ClusteredBubbleEntity
import com.umc.edison.data.model.bubble.KeywordBubbleEntity
import com.umc.edison.data.model.bubble.toData
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.bubble.Bubble
import com.umc.edison.domain.model.bubble.ClusteredBubble
import com.umc.edison.domain.model.bubble.KeywordBubble
import com.umc.edison.domain.repository.BubbleRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class BubbleRepositoryImpl @Inject constructor(
    private val bubbleLocalDataSource: BubbleLocalDataSource,
    private val bubbleRemoteDataSource: BubbleRemoteDataSource,
    private val resourceFactory: FlowBoundResourceFactory
) : BubbleRepository {
    // CREATE
    override fun addBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.addBubbles(bubbles.toData()) },
            remoteSync = {
                bubbles.map { bubble ->
                    val newBubble = bubbleLocalDataSource.getActiveBubble(bubble.id)
                    bubbleRemoteDataSource.syncBubble(newBubble)
                }
            },
            onRemoteSuccess = { remoteData ->
                remoteData.map {
                    val bubble = bubbleLocalDataSource.getActiveBubble(it.id)
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun addBubble(bubble: Bubble): Flow<DataResource<Bubble>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.addBubble(bubble.toData()) },
            remoteSync = {
                val newBubble = bubbleLocalDataSource.getActiveBubble(bubble.id)
                bubbleRemoteDataSource.syncBubble(newBubble)
            },
            onRemoteSuccess = { remoteData ->
                val bubble = bubbleLocalDataSource.getActiveBubble(remoteData.id)
                bubbleLocalDataSource.markAsSynced(bubble)
            }
        )

    // READ
    override fun getAllActiveBubbles(): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getAllActiveBubbles() }
        )

    override fun getAllClusteredBubbles(): Flow<DataResource<List<ClusteredBubble>>> =
        resourceFactory.local(
            dataAction = {
                val remoteResponse = bubbleRemoteDataSource.getAllClusteredBubbles()

                val clusteredBubbles = mutableListOf<ClusteredBubbleEntity>()
                remoteResponse.map {
                    try {
                        val bubble = bubbleLocalDataSource.getActiveBubble(it.id)
                        clusteredBubbles.add(
                            ClusteredBubbleEntity(
                                bubble = bubble,
                                x = it.x,
                                y = it.y,
                                group = it.group
                            )
                        )
                    } catch (e: Exception) {
                        // 로컬에 없는 버블은 무시
                        Log.d("BubbleRepositoryImpl", "getAllClusteredBubbles: ${e.message}")
                    }
                }

                clusteredBubbles
            }
        )

    override fun getKeywordBubbles(keyword: String): Flow<DataResource<List<KeywordBubble>>> =
        resourceFactory.remote(
            dataAction = {
                val remoteResponse = bubbleRemoteDataSource.getKeywordBubbles(keyword)

                remoteResponse.map { responseItem ->
                    try {
                        val bubble = bubbleLocalDataSource.getActiveBubble(responseItem.id)
                        KeywordBubbleEntity(
                            bubble = bubble,
                            similarity = responseItem.similarity
                        )
                    } catch (e: Exception) {
                        // 로컬에 없는 버블은 무시
                        Log.d("BubbleRepositoryImpl", "getKeywordBubbles: ${e.message}")
                        null
                    }
                }
            }
        )

    override fun getAllRecentBubbles(dayBefore: Int): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getAllRecentBubbles(dayBefore) }
        )

    override fun getAllTrashedBubbles(): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getAllTrashedBubbles() }
        )

    override fun getActiveBubble(id: String): Flow<DataResource<Bubble>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getActiveBubble(id) }
        )

    override fun getBubblesByLabel(labelId: String): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getBubblesByLabelId(labelId) }
        )

    override fun getBubblesWithoutLabel(): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getBubblesWithoutLabel() }
        )

    override fun searchBubbles(query: String): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getSearchBubbleResults(query) }
        )

    // UPDATE
    override fun recoverBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = {
                val updatedBubbles = bubbles.toData().map { bubble ->
                    bubble.copy(
                        isTrashed = false,
                        isDeleted = false,
                        deletedAt = null,
                    )
                }
                bubbleLocalDataSource.updateBubbles(updatedBubbles)
            },
            remoteSync = {
                bubbles.map { bubble ->
                    val updatedBubble = bubbleLocalDataSource.getActiveBubble(bubble.id)
                    bubbleRemoteDataSource.syncBubble(updatedBubble)
                }
            },
            onRemoteSuccess = { remoteData ->
                remoteData.map { remote ->
                    val bubble = bubbleLocalDataSource.getActiveBubble(remote.id)
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun updateBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.updateBubbles(bubbles.toData()) },
            remoteSync = {
                bubbles.map { bubble ->
                    val updatedBubble = bubbleLocalDataSource.getActiveBubble(bubble.id)
                    bubbleRemoteDataSource.syncBubble(updatedBubble)
                }
            },
            onRemoteSuccess = { remoteData ->
                remoteData.map { remote ->
                    val bubble = bubbleLocalDataSource.getActiveBubble(remote.id)
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun updateBubble(bubble: Bubble): Flow<DataResource<Bubble>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.updateBubble(bubble.toData()) },
            remoteSync = {
                val updatedBubble = bubbleLocalDataSource.getActiveBubble(bubble.id)
                bubbleRemoteDataSource.syncBubble(updatedBubble)
            },
            onRemoteSuccess = { remoteData ->
                val bubble = bubbleLocalDataSource.getActiveBubble(remoteData.id)
                bubbleLocalDataSource.markAsSynced(bubble)
            }
        )

    // DELETE
    override fun deleteBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = {
                val updatedBubbles = bubbles.toData().map { bubble ->
                    bubble.copy(
                        isDeleted = true,
                        deletedAt = Date(),
                    )
                }
                bubbleLocalDataSource.updateBubbles(updatedBubbles)
            },
            remoteSync = {
                bubbles.map { bubble ->
                    val deletedBubble = bubbleLocalDataSource.getRawBubble(bubble.id)
                    bubbleRemoteDataSource.syncBubble(deletedBubble)
                }
            },
            onRemoteSuccess = { deletedBubbles ->
               val localBubbles = deletedBubbles.map { remote -> bubbleLocalDataSource.getRawBubble(remote.id) }
                bubbleLocalDataSource.deleteBubbles(localBubbles)
            }
        )

    override fun trashBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = {
                bubbleLocalDataSource.trashBubbles(bubbles.toData())
            },
            remoteSync = {
                bubbles.map { bubble ->
                    val trashedBubble = bubbleLocalDataSource.getRawBubble(bubble.id)
                    bubbleRemoteDataSource.syncBubble(trashedBubble)
                }
            },
            onRemoteSuccess = { remoteData ->
                remoteData.map { remote ->
                    val bubble = bubbleLocalDataSource.getRawBubble(remote.id)
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )
}
