package com.umc.edison.presentation.login

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.umc.edison.domain.model.identity.IdentityCategory
import com.umc.edison.domain.usecase.identity.GetIdentityByCategoryUseCase
import com.umc.edison.domain.usecase.user.GoogleSignUpUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.IdentityModel
import com.umc.edison.presentation.model.KeywordModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.ui.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IdentityTestViewModel @Inject constructor(
    toastManager: ToastManager,
    private val googleSignUpUseCase: GoogleSignUpUseCase,
    private val getIdentityByCategoryUseCase: GetIdentityByCategoryUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(toastManager) {

    private val _uiState = MutableStateFlow(IdentityTestState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        val idToken: String = savedStateHandle.get<String>("idToken") ?: ""
        val nickname: String = savedStateHandle.get<String>("nickname") ?: ""

        _uiState.update { it.copy(idToken = idToken, nickname = nickname) }

        getIdentityCategoryForTab(0)
    }

    fun updateTabIndex(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
        getIdentityCategoryForTab(index)
    }

    private fun getIdentityCategoryForTab(index: Int) {
        val category = when (index) {
            0 -> IdentityCategory.EXPLAIN
            1 -> IdentityCategory.FIELD
            2 -> IdentityCategory.ENVIRONMENT
            3 -> IdentityCategory.INSPIRATION
            else -> return
        }
        getIdentityKeyWords(category)
    }

    private fun getIdentityKeyWords(identityCategory: IdentityCategory) {
        collectDataResource(
            flow = getIdentityByCategoryUseCase(
                category = identityCategory,
            ),
            onSuccess = { identity ->
                val model = identity.toPresentation()
                _uiState.update { state ->
                    state.copy(
                        identities = state.identities.toMutableMap().apply {
                            // 카테고리별로 IdentityModel 저장
                            put(identityCategory, model)
                        }
                    )
                }
            },
        )
    }


    fun toggleIdentityKeyword(keyword: KeywordModel) {
        val state = uiState.value
        val category = state.currentCategory
        val currentIdentity = state.identities[category] ?: IdentityModel.DEFAULT
        val selected = currentIdentity.selectedKeywords

        val updated = if (selected.contains(keyword)) {
            selected - keyword
        } else {
            if (selected.size >= 5) {
                showToast("최대 5개의 키워드를 선택할 수 있습니다.")
                return
            }
            selected + keyword
        }

        _uiState.update {
            it.copy(
                identities = it.identities.toMutableMap().apply {
                    put(category, currentIdentity.copy(selectedKeywords = updated))
                }
            )
        }
    }

    fun setIdentityTestResult(
        pagerState: PagerState,
        coroutineScope: CoroutineScope,
    ) {
        val state = uiState.value
        val current = state.currentIdentity

        if (current.selectedKeywords.isEmpty()) {
            showToast("키워드를 한 개 이상 선택해 주세요.")
            return
        }

        coroutineScope.launch {
            val currentPage = pagerState.currentPage
            if (currentPage < pagerState.pageCount - 1) {
                pagerState.animateScrollToPage(currentPage + 1)
            }
        }
    }


    fun submitIdentityTestResult(navController: NavHostController) {
        val state = uiState.value


        if (state.idToken.isBlank()) {
            showToast("로그인 정보가 없습니다. 다시 시도해주세요.")
            return
        }

        if (state.nickname.isBlank()) {
            showToast("닉네임 정보가 없습니다. 다시 시도해주세요.")
            return
        }

        if (state.currentIdentity.selectedKeywords.isEmpty()) {
            showToast("키워드를 한 개 이상 선택해 주세요.")
            return
        }

        val allIdentities = state.identities.values.map { it.toDomain() }

        collectDataResource(
            flow = googleSignUpUseCase(
                idToken = state.idToken,
                nickname = state.nickname,
                identities = allIdentities
            ),
            onSuccess = {
                viewModelScope.launch {
                    navController.navigate(NavRoute.MyEdison.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                }
            },
        )
    }
}
