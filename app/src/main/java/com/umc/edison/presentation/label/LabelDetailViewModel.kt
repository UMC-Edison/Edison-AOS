package com.umc.edison.presentation.label

import androidx.lifecycle.SavedStateHandle
import com.umc.edison.domain.usecase.bubble.GetBubblesByLabelUseCase
import com.umc.edison.domain.usecase.bubble.GetBubblesWithoutLabelUseCase
import com.umc.edison.domain.usecase.bubble.MoveBubblesToOtherLabelUseCase
import com.umc.edison.domain.usecase.bubble.TrashBubblesUseCase
import com.umc.edison.domain.usecase.label.GetAllLabelsUseCase
import com.umc.edison.domain.usecase.label.GetLabelUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.baseBubble.BaseBubbleViewModel
import com.umc.edison.presentation.baseBubble.LabelDetailMode
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.presentation.model.toPresentation
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
) : BaseBubbleViewModel<LabelDetailMode, LabelDetailState>(toastManager) {
    override val _uiState = MutableStateFlow(LabelDetailState.DEFAULT)
    override val uiState = _uiState.asStateFlow()

    private val labelId: String? = savedStateHandle["labelId"]

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
