package com.umc.edison.data.model

interface DataMapper<DomainModel> {
    fun toDomain(): DomainModel
}
