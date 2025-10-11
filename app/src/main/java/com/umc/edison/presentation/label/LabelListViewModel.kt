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
    private val getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
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
                _uiState.update { it.copy(labels = labels.distinctBy { it.id }.toPresentation()) }
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

        _uiState.value.labels.forEach {
            if (it.id.isNullOrEmpty()) return@forEach

            collectDataResource(
                flow = getBubblesByLabelUseCase(it.id),
                onSuccess = { bubbles ->
                    _uiState.update { uiState ->
                        uiState.copy(
                            labels = uiState.labels.map { label ->
                                if (label.id == it.id) {
                                    label.copy(bubbleCnt = bubbles.size)
                                } else {
                                    label
                                }
                            }
                        )
                    }
                },
                onComplete = {
                    // 버블 개수 많은 순으로 정렬
                    _uiState.update {
                        it.copy(
                            labels = it.labels.sortedByDescending { it.bubbleCnt }
                        )
                    }
                }
            )
        }
    }

    private fun fetchBubblesWithoutLabel() {
        collectDataResource(
            flow = getBubblesWithoutLabelUseCase(),
            onSuccess = { bubbles ->
                _uiState.update {
                    val labels = listOf(
                        LabelModel.DEFAULT.copy(bubbleCnt = bubbles.size)
                    ) + it.labels

                    it.copy(
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
