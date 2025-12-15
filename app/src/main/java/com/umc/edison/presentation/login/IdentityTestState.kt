package com.umc.edison.presentation.login

import com.umc.edison.domain.model.identity.IdentityCategory
import com.umc.edison.presentation.model.IdentityModel

data class IdentityTestState(
    val selectedTabIndex: Int,
    val idToken: String,
    val nickname: String,
    val identities: Map<IdentityCategory, IdentityModel> = mapOf(
        IdentityCategory.EXPLAIN to IdentityModel.DEFAULT,
        IdentityCategory.FIELD to IdentityModel.DEFAULT,
        IdentityCategory.ENVIRONMENT to IdentityModel.DEFAULT,
        IdentityCategory.INSPIRATION to IdentityModel.DEFAULT,
    )
) {
    val currentCategory: IdentityCategory
        get() = when (selectedTabIndex) {
            0 -> IdentityCategory.EXPLAIN
            1 -> IdentityCategory.FIELD
            2 -> IdentityCategory.ENVIRONMENT
            3 -> IdentityCategory.INSPIRATION
            else -> IdentityCategory.NONE
        }

    val currentIdentity: IdentityModel
        get() = identities[currentCategory] ?: IdentityModel.DEFAULT

    companion object {
        val DEFAULT = IdentityTestState(
            selectedTabIndex = 0,
            idToken = "",
            nickname = "",
        )
    }
}
