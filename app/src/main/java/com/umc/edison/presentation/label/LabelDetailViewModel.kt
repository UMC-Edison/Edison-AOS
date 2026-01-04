package com.umc.edison.presentation.label

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import com.umc.edison.domain.usecase.bubble.GetBubblesByLabelUseCase
import com.umc.edison.domain.usecase.bubble.GetBubblesWithoutLabelUseCase
import com.umc.edison.domain.usecase.bubble.MoveBubblesToOtherLabelUseCase
import com.umc.edison.domain.usecase.bubble.TrashBubblesUseCase
import com.umc.edison.domain.usecase.label.GetAllLabelsUseCase
import com.umc.edison.domain.usecase.label.GetLabelUseCase
import com.umc.edison.domain.usecase.onboarding.GetHasSeenOnboardingUseCase
import com.umc.edison.domain.usecase.onboarding.SetHasSeenOnboardingUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.baseBubble.BaseBubbleViewModel
import com.umc.edison.presentation.baseBubble.LabelDetailMode
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LabelDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    toastManager: ToastManager,
    private val getBubblesByLabelUseCase: GetBubblesByLabelUseCase,
    private val getBubblesWithoutLabelUseCase: GetBubblesWithoutLabelUseCase,
    private val getLabelUseCase: GetLabelUseCase,
    private val moveBubblesToOtherLabelUseCase: MoveBubblesToOtherLabelUseCase,
    private val getAllLabelsUseCase: GetAllLabelsUseCase,
    override val trashBubblesUseCase: TrashBubblesUseCase,
    getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase,
) : BaseBubbleViewModel<LabelDetailMode, LabelDetailState>(toastManager) {
    override val _uiState = MutableStateFlow(LabelDetailState.DEFAULT)
    override val uiState = _uiState.asStateFlow()

    private val labelId: String? = savedStateHandle["labelId"]

    private val _onboardingState = MutableStateFlow(LabelDetailOnboardingState.DEFAULT)
    val onboardingState = _onboardingState.asStateFlow()

    companion object {
        private const val SCREEN_NAME = "label_detail"
    }

    init {
        collectDataResource(
            flow = getHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(show = !hasSeen) }
            },
        )
    }

    private fun createTemporaryOnboardingBubbles() {
        val temporaryBubbles = listOf(
            BubbleModel.DEFAULT.copy(
                id = "temp_onboarding_label_1",
                title = "예시 버블 1",
                labels = emptyList(),
            ),
            BubbleModel.DEFAULT.copy(
                id = "temp_onboarding_label_2",
                title = "예시 버블 2",
                labels = emptyList(),
            ),
            BubbleModel.DEFAULT.copy(
                id = "temp_onboarding_label_3",
                title = "예시 버블 3",
                labels = emptyList(),
            )
        )
        
        _uiState.update {
            it.copy(
                bubbles = temporaryBubbles,
                hasTemporaryOnboardingBubbles = true
            )
        }
    }

    private fun clearTemporaryOnboardingBubbles() {
        if (_uiState.value.hasTemporaryOnboardingBubbles) {
            _uiState.update {
                it.copy(
                    bubbles = emptyList(),
                    hasTemporaryOnboardingBubbles = false
                )
            }
        }
    }

    fun setBubbleBounds(offset: Offset, size: IntSize) {
        _onboardingState.update {
            it.copy(
                bubbleBound = OnboardingPositionState(offset, size),
                isReady = true
            )
        }
    }

    fun dismissOnboarding() {
        collectDataResource(
            flow = setHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = {
                _onboardingState.update { it.copy(show = false) }
                clearTemporaryOnboardingBubbles()
            },
        )
    }

    fun fetchLabelDetail(id: String?) {
        _uiState.update { LabelDetailState.DEFAULT }

        if (id.isNullOrEmpty()) {
            collectDataResource(
                flow = getBubblesWithoutLabelUseCase(),
                onSuccess = { bubbles ->
                    val shuffledBubbles = bubbles.shuffled().toPresentation()
                    _uiState.update {
                        it.copy(
                            bubbles = shuffledBubbles,
                            label = LabelModel.DEFAULT.copy(bubbleCnt = shuffledBubbles.size),
                        )
                    }

                    if (shuffledBubbles.isEmpty() && _onboardingState.value.show) {
                        createTemporaryOnboardingBubbles()
                    }
                }
            )
        } else {
            collectDataResource(
                flow = getBubblesByLabelUseCase(id),
                onSuccess = { bubbles ->
                    val shuffledBubbles = bubbles.shuffled().toPresentation()
                    _uiState.update {
                        it.copy(
                            bubbles = shuffledBubbles,
                        )
                    }
                },
                onComplete = {
                    _uiState.update {
                        it.copy(
                            label = it.label.copy(
                                bubbleCnt = it.bubbles.size,
                            ),
                        )
                    }
                }
            )

            collectDataResource(
                flow = getLabelUseCase(id),
                onSuccess = { label ->
                    _uiState.update {
                        it.copy(
                            label = it.label.copy(
                                id = label.id,
                                name = label.name,
                                color = label.color,
                            ),
                        )
                    }
                },
            )
        }
    }

    fun getMovableLabels() {
        collectDataResource(
            flow = getAllLabelsUseCase(),
            onSuccess = { allLabels ->
                val movableLabels = allLabels.toPresentation().filter { label ->
                    _uiState.value.label.id != label.id
                }

                _uiState.update { it.copy(movableLabels = movableLabels) }
            },
        )
    }

    fun moveSelectedBubbles(label: LabelModel, showBottomNav: (Boolean) -> Unit) {
        collectDataResource(
            flow = moveBubblesToOtherLabelUseCase(
                bubbles = _uiState.value.selectedBubbles.toSet().map { it.toDomain() },
                moveFrom = _uiState.value.label.toDomain(),
                moveTo = label.toDomain()
            ),
            onSuccess = {
                updateEditMode(LabelDetailMode.NONE)
                showBottomNav(true)
                fetchLabelDetail(label.id)
            },
        )
    }

    fun refreshData() {
        fetchLabelDetail(labelId)
    }

    override fun refreshDataAfterDeletion() {
        refreshData()
    }
}
