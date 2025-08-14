package com.umc.edison.data.datasources

import com.umc.edison.data.model.label.LabelEntity

interface LabelRemoteDataSource {
    // CREATE
    suspend fun addLabel(label: LabelEntity): LabelEntity

    // READ
    suspend fun getAllLabels(): List<LabelEntity>

    // UPDATE
    suspend fun updateLabel(label: LabelEntity): LabelEntity
    suspend fun syncLabel(label: LabelEntity): LabelEntity

    // DELETE
    suspend fun deleteLabel(id: String): String
}