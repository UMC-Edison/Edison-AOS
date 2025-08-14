package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.LabelRemoteDataSource
import com.umc.edison.data.model.label.LabelEntity
import com.umc.edison.remote.api.LabelApiService
import com.umc.edison.remote.api.SyncApiService
import com.umc.edison.remote.model.label.toAddLabelRequest
import com.umc.edison.remote.model.label.toData
import com.umc.edison.remote.model.label.toUpdateLabelRequest
import com.umc.edison.remote.model.sync.toSyncLabelRequest
import javax.inject.Inject

class LabelRemoteDataSourceImpl @Inject constructor(
    private val labelApiService: LabelApiService,
    private val syncApiService: SyncApiService,
) : LabelRemoteDataSource{
    override suspend fun addLabel(label: LabelEntity): LabelEntity {
        return labelApiService.addLabel(label.toAddLabelRequest()).data.toData()
    }

    override suspend fun getAllLabels(): List<LabelEntity> {
        return labelApiService.getAllLabels().data.toData()
    }

    override suspend fun updateLabel(label: LabelEntity): LabelEntity {
        return labelApiService.updateLabel(label.id, label.toUpdateLabelRequest()).data.toData()
    }

    override suspend fun syncLabel(label: LabelEntity): LabelEntity {
        return syncApiService.syncLabel(label.toSyncLabelRequest()).data.toData()
    }

    override suspend fun deleteLabel(id: String): String {
        labelApiService.deleteLabel(id)

        return id
    }
}