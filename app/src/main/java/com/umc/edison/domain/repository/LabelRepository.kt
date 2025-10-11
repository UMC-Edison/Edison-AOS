package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.label.Label
import kotlinx.coroutines.flow.Flow

interface LabelRepository {
    // CREATE
    fun addLabel(label: Label): Flow<DataResource<Unit>>

    // READ
    fun getAllActiveLabels(): Flow<DataResource<List<Label>>>
    fun getLabel(id: String): Flow<DataResource<Label>>

    // UPDATE
    fun updateLabel(label: Label): Flow<DataResource<Unit>>

    // DELETE
    fun deleteLabel(id: String): Flow<DataResource<Unit>>
}