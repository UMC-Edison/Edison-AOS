package com.umc.edison.data.bound

import com.umc.edison.data.toDomain
import com.umc.edison.domain.DataResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

abstract class FLowBaseBoundResource<DomainType, DataType>(
    private val dataAction: suspend () -> DataType
) : Flow<DataResource<DomainType>> {
    protected open suspend fun fetchFromSource(
        collector: FlowCollector<DataResource<DomainType>>,
        successAction: (suspend (DataType) -> Unit)? = null,
    ) {
        collector.emit(DataResource.loading())
        try {
            val data = dataAction()
            collector.emit(DataResource.success(data.toDomain()))
            successAction?.invoke(data)
        } catch (e: Exception) {
            collector.emit(DataResource.error(e))
        }
    }
}
