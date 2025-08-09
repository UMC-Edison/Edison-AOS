package com.umc.edison.presentation.edison

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.umc.edison.domain.usecase.bubble.GetAllRecentBubblesUseCase
import com.umc.edison.domain.usecase.bubble.SearchBubblesUseCase
import com.umc.edison.domain.usecase.onboarding.GetHasSeenOnboardingUseCase
import com.umc.edison.domain.usecase.onboarding.SetHasSeenOnboardingUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MyEdisonViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getAllRecentBubblesUseCase: GetAllRecentBubblesUseCase,
    private val searchBubblesUseCase: SearchBubblesUseCase,
    getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase,
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(MyEdisonState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    private val _onboardingState = MutableStateFlow(MyEdisonOnboardingState.DEFAULT)
    val onboardingState = _onboardingState.asStateFlow()

    companion object {
        const val SCREEN_NAME = "my_edison"
    }

    init {
        collectDataResource(
            flow = getHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(show = !hasSeen) }
            },
        )
    }

    fun fetchBubbles() {
        collectDataResource(
            flow = getAllRecentBubblesUseCase(),
            onSuccess = { bubbles ->
                _uiState.update {
                    it.copy(bubbles = bubbles.toPresentation())
                }
            },
        )
    }

    fun fetchSearchBubbles(query: String) {
        collectDataResource(
            flow = searchBubblesUseCase(query),
            onSuccess = { bubbles ->
                _uiState.update { it.copy(searchResults = bubbles.toPresentation()) }

                if (bubbles.isEmpty()) {
                    showToast("검색 결과를 찾을 수 없습니다.")
                }
            },
            onLoading = {
                _uiState.update { it.copy(query = query) }
            },
        )
    }

    fun resetSearchResults() {
        _uiState.update { it.copy(searchResults = emptyList(), query = "") }
    }

    fun setHasSeenOnboarding() {
        collectDataResource(
            flow = setHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = {
                _onboardingState.update { it.copy(show = false) }
            },
        )
    }

    fun setBubbleInputBound(offset: Offset, size: IntSize) {
        _onboardingState.update {
            it.copy(
                bubbleInputBound = OnboardingPositionState(
                    offset,
                    size
                )
            )
        }
    }

    fun setNavBarPosition(idx: Int, offset: Offset, size: IntSize) {
        _onboardingState.update {
            val updatedNavBarPosition = it.myEdisonNavBarBounds.toMutableList()
            if (idx in updatedNavBarPosition.indices) {
                updatedNavBarPosition[idx] = OnboardingPositionState(offset, size)
            } else {
                updatedNavBarPosition.add(OnboardingPositionState(offset, size))
            }

            it.copy(myEdisonNavBarBounds = updatedNavBarPosition)
        }
    }
}
