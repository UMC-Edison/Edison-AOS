package com.umc.edison.presentation.label

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.umc.edison.domain.usecase.bubble.GetAllBubblesUseCase
import com.umc.edison.domain.usecase.bubble.GetBubblesByLabelUseCase
import com.umc.edison.domain.usecase.bubble.GetBubblesWithoutLabelUseCase
import com.umc.edison.domain.usecase.label.AddLabelUseCase
import com.umc.edison.domain.usecase.label.DeleteLabelUseCase
import com.umc.edison.domain.usecase.label.GetAllLabelsUseCase
import com.umc.edison.domain.usecase.label.UpdateLabelUseCase
import com.umc.edison.domain.usecase.onboarding.GetHasSeenOnboardingUseCase
import com.umc.edison.domain.usecase.onboarding.SetHasSeenOnboardingUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LabelListViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getAllLabelsUseCase: GetAllLabelsUseCase,
    private val getBubblesByLabelUseCase: GetBubblesByLabelUseCase,
    private val getBubblesWithoutLabelUseCase: GetBubblesWithoutLabelUseCase,
    private val addLabelUseCase: AddLabelUseCase,
    private val updateLabelUseCase: UpdateLabelUseCase,
    private val deleteLabelUseCase: DeleteLabelUseCase,
    private val getAllBubblesUseCase: GetAllBubblesUseCase,
    getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(LabelListState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    private val _onboardingState = MutableStateFlow(LabelListOnboardingState.DEFAULT)
    val onboardingState = _onboardingState.asStateFlow()

    companion object {
        const val SCREEN_NAME = "label_list"
    }

    init {
        collectDataResource(
            flow = getHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(show = !hasSeen) }
            }
        )
    }

    fun fetchLabels() {
        _uiState.update { LabelListState.DEFAULT }
        collectDataResource(
            flow = getAllLabelsUseCase(),
            onSuccess = { labels ->
                _uiState.update { uiState ->
                    uiState.copy(labels = labels.toPresentation())
                }
            },
            onComplete = {
                fetchBubblesWithoutLabel()
            }
        )
    }

    fun fetchTotalBubbleCount() {
        collectDataResource(
            flow = getAllBubblesUseCase(),
            onSuccess = { bubbles ->
                _uiState.update { it.copy(bubbleCount = bubbles.size) }
            }
        )
    }

    private fun fetchBubblesByLabel() {
        val labelsWithId = _uiState.value.labels.filter { !it.id.isNullOrEmpty() }
        var completedCount = 0
        
        labelsWithId.forEach { label ->
            collectDataResource(
                flow = getBubblesByLabelUseCase(label.id!!),
                onSuccess = { bubbles ->
                    _uiState.update { uiState ->
                        uiState.copy(
                            labels = uiState.labels.map { 
                                if (it.id == label.id) {
                                    it.copy(bubbleCnt = bubbles.size)
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                onComplete = {
                    completedCount++
                    // 모든 라벨의 버블 개수를 가져온 후 한 번만 정렬
                    if (completedCount == labelsWithId.size) {
                        _uiState.update { uiState ->
                            uiState.copy(
                                labels = uiState.labels.sortedWith(
                                    compareByDescending<LabelModel> { it.id == null }
                                        .thenByDescending { it.bubbleCnt }
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    private fun fetchBubblesWithoutLabel() {
        collectDataResource(
            flow = getBubblesWithoutLabelUseCase(),
            onSuccess = { bubbles ->
                _uiState.update { uiState ->
                    val labels = listOf(
                        LabelModel.DEFAULT.copy(bubbleCnt = bubbles.size)
                    ) + uiState.labels

                    uiState.copy(
                        labels = labels.distinctBy { it.id }
                    )
                }
                fetchBubblesByLabel()
            }
        )
    }

    fun updateEditMode(labelEditMode: LabelEditMode) {
        if (labelEditMode == LabelEditMode.ADD) {
            _uiState.update { it.copy(selectedLabel = LabelListState.DEFAULT.selectedLabel) }
        }
        _uiState.update { it.copy(labelEditMode = labelEditMode) }
    }

    fun updateSelectedLabel(label: LabelModel) {
        _uiState.update { it.copy(selectedLabel = label) }
    }

    fun confirmLabelModal(label: LabelModel) {
        if (uiState.value.labelEditMode == LabelEditMode.ADD) {
            collectDataResource(
                flow = addLabelUseCase(label.toDomain()),
                onSuccess = {
                    updateEditMode(LabelEditMode.NONE)
                    _uiState.update { it.copy(selectedLabel = LabelListState.DEFAULT.selectedLabel) }
                },
                onComplete = {
                    fetchLabels()
                }
            )
        } else if (uiState.value.labelEditMode == LabelEditMode.EDIT) {
            collectDataResource(
                flow = updateLabelUseCase(label.toDomain()),
                onSuccess = {
                    updateEditMode(LabelEditMode.NONE)
                    _uiState.update { it.copy(selectedLabel = LabelListState.DEFAULT.selectedLabel) }
                },
                onComplete = {
                    fetchLabels()
                }
            )
        }
    }

    fun deleteSelectedLabel() {
        collectDataResource(
            flow = deleteLabelUseCase(_uiState.value.selectedLabel.toDomain().id),
            onSuccess = {
                updateEditMode(LabelEditMode.NONE)
                _uiState.update {
                    it.copy(
                        labels = it.labels.filter { label -> label.id != it.selectedLabel.id },
                        selectedLabel = LabelListState.DEFAULT.selectedLabel
                    )
                }
            },
            onComplete = {
                fetchLabels()
            }
        )
    }

    fun setHasSeenOnboarding() {
        collectDataResource(
            flow = setHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = {
                _onboardingState.update { it.copy(show = false) }
            }
        )
    }

    fun setLabelListItemBound(offset: Offset, size: IntSize) {
        _onboardingState.update { it.copy(labelBound = OnboardingPositionState(offset, size)) }
    }
}
