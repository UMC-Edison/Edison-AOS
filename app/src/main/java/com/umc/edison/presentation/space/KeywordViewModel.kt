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

    fun fetchKeywordBubbles(keyword: String) {
        if (keyword.isBlank()) {
            _uiState.update { it.copy(bubbles = emptyList()) }
            return
        }

        collectDataResource(
            flow = getKeywordBubbleUsecase(keyword),
            onSuccess = { domainBubbles ->
                // [수정] similarity가 높은 순으로 정렬하여 상태를 업데이트합니다.
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

