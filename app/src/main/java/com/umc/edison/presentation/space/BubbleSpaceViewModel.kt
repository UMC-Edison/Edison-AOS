package com.umc.edison.presentation.space

import com.umc.edison.domain.usecase.bubble.SearchBubblesUseCase
import com.umc.edison.domain.usecase.user.GetLogInStateUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BubbleSpaceViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getLogInStateUseCase: GetLogInStateUseCase,
    private val searchBubblesUseCase: SearchBubblesUseCase

) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(BubbleSpaceState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    fun checkLoginState() {
        collectDataResource(
            flow = getLogInStateUseCase(),
            onSuccess = { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            },
        )
    }

    fun updateBubbleSpaceMode(mode: BubbleSpaceMode) {
        _uiState.update { it.copy(mode = mode) }
    }


    fun selectBubble(bubble: BubbleModel?) {
        _uiState.update { it.copy(selectedBubble = bubble) }
    }

    fun searchBubbles() {
        if (_uiState.value.query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        collectDataResource(
            flow = searchBubblesUseCase(_uiState.value.query),
            onSuccess = { bubbles ->
                _uiState.update { it.copy(searchResults = bubbles.toPresentation()) }
            },
            onComplete = {
                if (uiState.value.searchResults.isEmpty()) {
                    showToast("검색 결과를 찾을 수 없습니다.")
                }
            }
        )
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }

        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun switchToKeywordMap() {
        _uiState.update { it.copy(mode = BubbleSpaceMode.KEYWORD) }
    }

    fun switchToGraph() {
        _uiState.update { it.copy(mode = BubbleSpaceMode.DEFAULT) }
    }



}
