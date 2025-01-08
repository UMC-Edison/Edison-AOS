package com.umc.edison.data

internal interface DataMapper<DomainModel> {
    fun toDomain(): DomainModel
}

internal fun <EntityModel, DomainModel> EntityModel.toDomain(): DomainModel {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is DataMapper<*> -> toDomain()
        is List<*> -> map {
            val domainModel: DomainModel = it.toDomain()
            domainModel
        }

        is Unit -> this
        is Boolean -> this
        is Int -> this
        is String -> this
        is Byte -> this
        is Short -> this
        is Long -> this
        is Char -> this
        else -> {
            throw IllegalArgumentException("DataModel은 DataMapper<>, List<DataMapper<>>, Unit중 하나여야 합니다.")
        }
    } as DomainModel
}

internal fun <EntityModel : DataMapper<DomainModel>, DomainModel> List<EntityModel>.toDomain(): List<DomainModel> {
    return map(DataMapper<DomainModel>::toDomain)
}
