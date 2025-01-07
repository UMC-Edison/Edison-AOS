package com.umc.edison.local.model

interface LocalMapper<DataModel> {
    fun toData(): DataModel
}