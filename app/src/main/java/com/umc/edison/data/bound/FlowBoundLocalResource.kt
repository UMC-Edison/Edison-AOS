package com.umc.edison.data.bound

import android.util.Log
import com.umc.edison.domain.DataResource
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector

class FlowBoundLocalResource<DomainType, DataType>(
    localDataAction: suspend () -> DataType,
    private val getNotSyncedAction: suspend () -> List<DataType>,
    private val syncRemoteAction: suspend (List<DataType>) -> Unit,
) : FLowBaseBoundResource<DomainType, DataType>(localDataAction) {

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<DataResource<DomainType>>) {
        try {
            fetchFromSource(collector)

            val notSyncedData = getNotSyncedAction()
            if (notSyncedData.isNotEmpty()) {
                syncRemoteAction(notSyncedData)
            }
        } catch (e: Exception) {
            Log.e("FlowBoundLocalResource", "sync data: $e")
        }
    }
}
