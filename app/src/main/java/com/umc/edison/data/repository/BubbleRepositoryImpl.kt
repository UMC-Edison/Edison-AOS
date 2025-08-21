package com.umc.edison.data.repository

import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.BubbleLocalDataSource
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.model.bubble.ClusteredBubbleEntity
import com.umc.edison.data.model.bubble.KeywordBubbleEntity
import com.umc.edison.data.model.bubble.SimilarityBubbleEntity
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
            remoteSync = { bubbleRemoteDataSource.addBubbles(bubbles.toData()) },
            onRemoteSuccess = { remoteData ->
                for (bubble in remoteData) {
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun addBubble(bubble: Bubble): Flow<DataResource<Bubble>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.addBubble(bubble.toData()) },
            remoteSync = { bubbleRemoteDataSource.addBubble(bubble.toData()) },
            onRemoteSuccess = { remoteData -> bubbleLocalDataSource.markAsSynced(remoteData) }
        )

    // READ
    override fun getAllBubbles(): Flow<DataResource<List<Bubble>>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getAllBubbles() }
        )

    override fun getAllClusteredBubbles(): Flow<DataResource<List<ClusteredBubble>>> =
        resourceFactory.local(
            dataAction = {
                val remoteResponse = bubbleRemoteDataSource.getAllClusteredBubbles()

                val clusteredBubbles = mutableListOf<ClusteredBubbleEntity>()
                remoteResponse.map {
                    val bubble = bubbleLocalDataSource.getBubble(it.id)
                    clusteredBubbles.add(
                        ClusteredBubbleEntity(
                            bubble = bubble,
                            x = it.x,
                            y = it.y,
                            group = it.group
                        )
                    )
                }

                clusteredBubbles
            }
        )

    override fun getKeywordBubbles(keyword: String): Flow<DataResource<List<KeywordBubble>>> =
    // 제공해주신 예시와 동일한 resourceFactory를 사용합니다.
        // 네트워크 통신이므로 .network 나 .remote 와 같은 다른 메소드가 있다면 그것을 사용하는 것이 더 적절할 수 있습니다.
        resourceFactory.local(
            dataAction = {

                // 1. RemoteDataSource를 통해 API를 호출하고 키워드를 전달합니다.
                val remoteResponse = bubbleRemoteDataSource.getKeywordBubbles(keyword)
                val keywordBubbles = mutableListOf<KeywordBubbleEntity>()

                // 2. API로부터 받은 데이터(BubbleEntity 리스트)를
                //    화면에서 사용할 데이터(KeywordBubble 리스트)로 변환(매핑)합니다.
                remoteResponse.map {
                    val bubble = bubbleLocalDataSource.getBubble(it.id)
                    keywordBubbles.add(
                        KeywordBubbleEntity(
                            bubble=bubble,
                            similarity = it.similarity
                        )
                    )
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

    override fun getBubble(id: String): Flow<DataResource<Bubble>> =
        resourceFactory.local(
            dataAction = { bubbleLocalDataSource.getBubble(id) }
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
            remoteSync = { bubbleRemoteDataSource.recoverBubbles(bubbles.toData()) },
            onRemoteSuccess = { remoteData ->
                for (bubble in remoteData) {
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun updateBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.updateBubbles(bubbles.toData()) },
            remoteSync = { bubbleRemoteDataSource.updateBubbles(bubbles.toData()) },
            onRemoteSuccess = { remoteData ->
                for (bubble in remoteData) {
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )

    override fun updateBubble(bubble: Bubble): Flow<DataResource<Bubble>> =
        resourceFactory.sync(
            localAction = { bubbleLocalDataSource.updateBubble(bubble.toData()) },
            remoteSync = { bubbleRemoteDataSource.updateBubble(bubble.toData()) },
            onRemoteSuccess = { remoteData -> bubbleLocalDataSource.markAsSynced(remoteData) }
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
            remoteSync = { bubbleRemoteDataSource.deleteBubbles(bubbles.toData()) },
            onRemoteSuccess = { remoteData -> bubbleLocalDataSource.deleteBubbles(remoteData) }
        )

    override fun trashBubbles(bubbles: List<Bubble>): Flow<DataResource<Unit>> =
        resourceFactory.sync(
            localAction = {
                val updatedBubbles = bubbles.toData().map { bubble ->
                    bubble.copy(
                        isTrashed = true,
                        isDeleted = false,
                        deletedAt = Date(),
                    )
                }
                bubbleLocalDataSource.updateBubbles(updatedBubbles)
            },
            remoteSync = { bubbleRemoteDataSource.trashBubbles(bubbles.toData()) },
            onRemoteSuccess = { remoteData ->
                for (bubble in remoteData) {
                    bubbleLocalDataSource.markAsSynced(bubble)
                }
            }
        )
}