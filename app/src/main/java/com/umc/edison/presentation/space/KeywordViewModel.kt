package com.umc.edison.presentation.space

import com.umc.edison.domain.usecase.bubble.GetKeywordBubbleUsecase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.KeywordBubbleModel
import com.umc.edison.presentation.model.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
@HiltViewModel
class KeywordViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getKeywordBubbleUsecase: GetKeywordBubbleUsecase,

): BaseViewModel(toastManager){


    private val _uiState = MutableStateFlow(KeywordMapState.DEFAULT)
    val uiState = _uiState.asStateFlow()



    fun showKeywordBar() {
        _uiState.update { it.copy(isBarVisible = true) }
    }

    fun hideKeywordBar() {
        _uiState.update { it.copy(isBarVisible = false) }
    }


    fun showKeywordToast() {
        showToast("키워드는 20자까지 입력 가능합니다.")
    }

    fun fetchKeywordBubbles(keywordToSearch: String) {
        if (keywordToSearch.isBlank()) {
            showToast("키워드를 입력 후 검색해주세요.")
            return
        }
        _uiState.update { it.copy(keyword = keywordToSearch) }

        collectDataResource(
            flow = getKeywordBubbleUsecase(keywordToSearch),
            onSuccess = { domainBubbles ->
                val sortedBubbles = domainBubbles.sortedByDescending { it.similarity }
                _uiState.update { it.copy(bubbles = sortedBubbles.toPresentation()) }
            },
            onComplete = {
                if (_uiState.value.bubbles.isEmpty()) {
                    showToast("관련된 버블을 찾을 수 없습니다.")
                }
            }
        )
    }

    fun selectBubble(bubble: KeywordBubbleModel) {
        _uiState.update { it.copy(selectedBubble = bubble) }
    }

    fun dismissBubble() {
        _uiState.update { it.copy(selectedBubble = null) }
    }





}

