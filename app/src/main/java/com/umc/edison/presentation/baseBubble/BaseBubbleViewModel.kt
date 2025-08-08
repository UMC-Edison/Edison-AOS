package com.umc.edison.presentation.baseBubble

import com.umc.edison.domain.usecase.bubble.TrashBubblesUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseBubbleViewModel<M : BaseBubbleMode, S : BaseBubbleState<M>>(
    toastManager: ToastManager,
) : BaseViewModel(toastManager) {
    protected abstract val _uiState: MutableStateFlow<S>
    open val uiState = _uiState.asStateFlow()

    abstract val trashBubblesUseCase: TrashBubblesUseCase

    // 컨텐츠 블럭 추가
    fun addContentBlock(type: ContentType) {
        val newBlock = ContentBlockModel(type, "", uiState.value.contentBlocks.size)
        _uiState.update { currentState ->
            val updatedContentBlocks = currentState.contentBlocks.apply {
                add(newBlock)  // LinkedList에 새 컨텐츠 블럭 추가
            }

            currentState.copyState(contentBlocks = updatedContentBlocks) as S
        }
    }

    // 컨텐츠 블럭 삭제
    fun deleteContentBlock(contentBlock: ContentBlockModel) {
        _uiState.update { currentState ->
            val updatedContentBlocks = currentState.contentBlocks.apply {
                remove(contentBlock)  // LinkedList에서 컨텐츠 블럭 삭제
            }

            currentState.copyState(contentBlocks = updatedContentBlocks) as S
        }
    }

    fun updateEditMode(mode: BaseBubbleMode) {
        if (mode == BaseBubbleMode.NONE) {
            _uiState.update {
                it.copyState(
                    mode = mode,
                    selectedBubbles = emptyList()
                ) as S
            }
        } else {
            _uiState.update {
                it.copyState(mode = mode) as S
            }
        }
    }

    fun selectBubble(bubble: BubbleModel) {
        _uiState.update {
            it.copyState(selectedBubbles = listOf(bubble)) as S
        }
    }

    fun toggleSelectBubble(bubble: BubbleModel) {
        _uiState.update {
            if (it.selectedBubbles.contains(bubble)) {
                it.copyState(selectedBubbles = it.selectedBubbles - bubble) as S
            } else {
                it.copyState(selectedBubbles = it.selectedBubbles + bubble) as S
            }
        }
    }

    fun deleteSelectedBubbles(showBottomNav: (Boolean) -> Unit) {
        collectDataResource(
            flow = trashBubblesUseCase(
                _uiState.value.selectedBubbles.toSet().map { it.toDomain() }
            ),
            onSuccess = {
                updateEditMode(BaseBubbleMode.NONE)
                showBottomNav(true)
                refreshDataAfterDeletion()
            },
        )
    }

    protected abstract fun refreshDataAfterDeletion()
}
