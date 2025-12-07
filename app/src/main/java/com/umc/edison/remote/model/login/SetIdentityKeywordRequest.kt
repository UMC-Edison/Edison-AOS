package com.umc.edison.remote.model.login

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.identity.IdentityCategoryEntity
import com.umc.edison.data.model.identity.IdentityEntity

data class SetIdentityKeywordRequest(
    @SerializedName("category")
    val category: String,
    @SerializedName("keywords")
    val keywords: List<Int>
)

fun IdentityEntity.toSetIdentityKeywordRequest(): SetIdentityKeywordRequest {

    val categoryId = when (this.category) {
        IdentityCategoryEntity.EXPLAIN -> "CATEGORY1"
        IdentityCategoryEntity.FIELD -> "CATEGORY2"
        IdentityCategoryEntity.ENVIRONMENT -> "CATEGORY3"
        IdentityCategoryEntity.INSPIRATION -> "CATEGORY4"
    }

    return SetIdentityKeywordRequest(
        category = categoryId,
        keywords = selectedKeywords.map { it.id }
    )
}