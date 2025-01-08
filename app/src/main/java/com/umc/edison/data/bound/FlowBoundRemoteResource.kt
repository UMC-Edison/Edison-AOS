package com.umc.edison.data.bound

import com.umc.edison.domain.DataResource
import kotlinx.coroutines.flow.FlowCollector

class FlowBoundRemoteResource<DomainType, DataType>(
    remoteDataAction: suspend () -> DataType,
) : FLowBaseBoundResource<DomainType, DataType>(remoteDataAction) {
    override suspend fun collect(collector: FlowCollector<DataResource<DomainType>>) {
        fetchFromSource(collector)
    }
}