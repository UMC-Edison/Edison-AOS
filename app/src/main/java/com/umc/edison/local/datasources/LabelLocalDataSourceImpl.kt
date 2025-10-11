package com.umc.edison.local.datasources

import android.util.Log
import com.umc.edison.data.datasources.LabelLocalDataSource
import com.umc.edison.data.model.label.LabelEntity
import com.umc.edison.local.model.LabelLocal
import com.umc.edison.local.model.toLocal
import com.umc.edison.local.room.RoomConstant
import com.umc.edison.local.room.dao.LabelDao
import javax.inject.Inject

class LabelLocalDataSourceImpl @Inject constructor(
    private val labelDao: LabelDao,
) : LabelLocalDataSource, BaseLocalDataSourceImpl<LabelLocal>(labelDao) {

    private val tableName = RoomConstant.getTableNameByClass(LabelLocal::class.java)

    // CREATE
    override suspend fun addLabel(label: LabelEntity) {
        insert(label.toLocal())
    }

    // READ
    override suspend fun getAllActiveLabels(): List<LabelEntity> {
        labelDao.getAllActiveLabels().forEach {
            Log.i("LabelLocalDataSourceImpl", "Label: ${it.uuid}, ${it.name}, ${it.color}")
        }
        return labelDao.getAllActiveLabels().map { it.toData() }
    }

    override suspend fun getLabel(id: String): LabelEntity {
        val localLabel = labelDao.getLabelById(id) ?: throw Exception("Label not found")

        return localLabel.toData()
    }

    override suspend fun getUnSyncedLabels(): List<LabelEntity> {
        return getAllUnSyncedRows(tableName).map { it.toData() }
    }

    // UPDATE
    override suspend fun markAsSynced(label: LabelEntity) {
        markAsSynced(tableName, label.id)
    }

    override suspend fun syncLabels(labels: List<LabelEntity>) {
        labels.map { syncLabel(it) }
    }

    override suspend fun updateLabel(label: LabelEntity, isSynced: Boolean) {
        update(label.toLocal(), tableName, isSynced)
    }

    private suspend fun syncLabel(label: LabelEntity) {
        try {
            val savedLabel = getLabel(label.id)
            if (!savedLabel.same(label)
                && savedLabel.updatedAt < label.updatedAt
            ) {
                updateLabel(label, true)
            }
        } catch (_: Exception) {
            addLabel(label)
        }
        markAsSynced(label)
    }

    // DELETE
    override suspend fun deleteLabel(id: String) {
        val label = getLabel(id)
        labelDao.delete(label.toLocal())
    }
}