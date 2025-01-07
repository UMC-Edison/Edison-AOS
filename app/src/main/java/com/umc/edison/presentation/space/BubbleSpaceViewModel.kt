package com.umc.edison.presentation.space

import com.umc.edison.domain.usecase.bubble.GetAllBubblesUseCase
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BubbleSpaceViewModel @Inject constructor(
    private val getAllBubblesUseCase: GetAllBubblesUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(BubbleSpaceState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        fetchBubbles()
    }

    private fun fetchBubbles() {
        collectDataResource(
            flow = getAllBubblesUseCase(),
            onSuccess = { bubbles ->
                _uiState.update { it.copy(bubbles = bubbles.toPresentation()) }
            },
            onError = { error ->
                _uiState.update { it.copy(error = error) }
            },
            onLoading = {
                _uiState.update { it.copy(isLoading = true) }
            },
            onComplete = {
                _uiState.update { it.copy(isLoading = false) }
            }
        )
    }
}