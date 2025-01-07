package com.umc.edison.data.repository

import com.umc.edison.data.datasources.BubbleSpaceLocalDataSource
import com.umc.edison.data.datasources.BubbleSpaceRemoteDataSource
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.Bubble
import com.umc.edison.domain.repository.BubbleSpaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class BubbleSpaceRepositoryImpl @Inject constructor(
    private val bubbleSpaceRemoteDataSource: BubbleSpaceRemoteDataSource,
    private val bubbleSpaceLocalDataSource: BubbleSpaceLocalDataSource
) : BubbleSpaceRepository {
    override fun getAllBubbles(): Flow<DataResource<List<Bubble>>> = flow {
        emit(DataResource.loading())
        try {
            val bubbles = bubbleSpaceRemoteDataSource.getAllBubbles().map { it.toDomain() }
            emit(DataResource.success(bubbles))
        } catch (e: IOException) {
            val bubbles = bubbleSpaceLocalDataSource.getAllBubbles().map { it.toDomain() }
            emit(DataResource.success(bubbles))
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }
}