package com.umc.edison.remote.model

interface RemoteMapper<DataModel> {
    fun toData(): DataModel
}