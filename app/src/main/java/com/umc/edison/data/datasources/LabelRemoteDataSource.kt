package com.umc.edison.data.datasources

import com.umc.edison.data.model.label.LabelEntity

interface LabelRemoteDataSource {
    // READ
    suspend fun getAllLabels(): List<LabelEntity>

    // UPDATE
    suspend fun syncLabel(label: LabelEntity): LabelEntity
}
