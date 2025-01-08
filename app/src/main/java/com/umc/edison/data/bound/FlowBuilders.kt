package com.umc.edison.data.bound

fun <DomainType, DataType> flowDataResource(
    localDataAction: suspend () -> DataType,
    getNotSyncedAction: suspend () -> List<DataType>,
    syncRemoteAction: suspend (List<DataType>) -> Unit,
) = FlowBoundLocalResource<DomainType, DataType>(
    localDataAction = localDataAction,
    getNotSyncedAction = getNotSyncedAction,
    syncRemoteAction = syncRemoteAction,
)

fun <DomainType, DataType> flowDataResource(
    remoteDataAction: suspend () -> DataType,
) = FlowBoundRemoteResource<DomainType, DataType>(
    remoteDataAction = remoteDataAction,
)
