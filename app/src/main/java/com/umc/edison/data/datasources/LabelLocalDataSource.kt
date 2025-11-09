package com.umc.edison.data.datasources

import com.umc.edison.data.model.label.LabelEntity

interface LabelLocalDataSource {
    // CREATE
    suspend fun addLabel(label: LabelEntity)

    // READ
    suspend fun getAllActiveLabels(): List<LabelEntity>
    suspend fun getLabel(id: String): LabelEntity
    suspend fun getUnSyncedLabels(): List<LabelEntity>

    // UPDATE
    suspend fun markAsSynced(label: LabelEntity)
    suspend fun syncLabels(labels: List<LabelEntity>)
    suspend fun updateLabel(label: LabelEntity, isSynced: Boolean = false)

    // DELETE
    suspend fun deleteLabel(id: String)
}