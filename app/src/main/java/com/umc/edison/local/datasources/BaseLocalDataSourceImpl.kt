package com.umc.edison.local.datasources

import androidx.sqlite.db.SimpleSQLiteQuery
import com.umc.edison.local.model.BaseSyncLocal
import com.umc.edison.local.room.dao.BaseSyncDao
import java.util.Date

open class BaseLocalDataSourceImpl<T : BaseSyncLocal>(
    private val baseDao: BaseSyncDao<T>
) {
    // CREATE
    suspend fun insert(entity: T): String {
        entity.createdAt = Date()
        entity.updatedAt = Date()
        entity.isSynced = false
        baseDao.insert(entity)
        return entity.uuid
    }

    // READ
    suspend fun getAllUnSyncedRows(tableName: String): List<T> {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName WHERE is_synced = 0")
        return baseDao.getAllUnSyncedRows(query)
    }

    // UPDATE
    suspend fun update(entity: T, tableName: String, isSynced: Boolean = false) {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName WHERE id = '${entity.uuid}'")
        baseDao.getById(query)?.let {
            entity.createdAt = it.createdAt
            entity.updatedAt = Date()
            entity.isSynced = isSynced
            baseDao.update(entity)
        }
    }

    suspend fun markAsSynced(tableName: String, id: String) {
        val date = Date()
        val query = SimpleSQLiteQuery("UPDATE $tableName SET is_synced = 1, updated_at = '$date' WHERE id = '$id'")
        baseDao.markAsSynced(query)
    }
}