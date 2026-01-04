package com.umc.edison.presentation.space

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.umc.edison.domain.usecase.bubble.GetAllClusteredBubblesUseCase
import com.umc.edison.domain.usecase.onboarding.GetHasSeenOnboardingUseCase
import com.umc.edison.domain.usecase.onboarding.SetHasSeenOnboardingUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toClusterModel
import com.umc.edison.presentation.model.toEdgeDataModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BubbleGraphViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getAllClusteredBubblesUseCase: GetAllClusteredBubblesUseCase,
    getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase,
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(BubbleGraphState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    private val _onboardingState = MutableStateFlow(BubbleGraphOnboardingState.DEFAULT)
    val onboardingState = _onboardingState.asStateFlow()

    companion object {
        const val SCREEN_NAME = "bubble_graph"
    }

    init {
        collectDataResource(
            flow = getHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(show = !hasSeen) }
            },
        )
    }

    fun fetchClusteredBubbles() {
        collectDataResource(
            flow = getAllClusteredBubblesUseCase(),
            onSuccess = { clusteredBubbles ->
                val bubbles =
                    clusteredBubbles.toPresentation().sortedByDescending { it.bubble.date }
                val edges = bubbles.toEdgeDataModel()
                val clusters = bubbles.toClusterModel()

                _uiState.update {
                    it.copy(
                        bubbles = bubbles,
                        edges = edges,
                        clusters = clusters
                    )
                }
            },
        )
    }

    fun setKeywordMapButtonBounds(offset: Offset, size: IntSize) {
        _onboardingState.update {
            it.copy(keywordMapButtonBound = OnboardingPositionState(offset, size))
        }
    }

    fun dismissOnboarding() {
        collectDataResource(
            flow = setHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = {
                _onboardingState.update { it.copy(show = false) }
            },
        )
    }
}
