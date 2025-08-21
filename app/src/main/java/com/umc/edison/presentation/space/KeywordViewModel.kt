package com.umc.edison.presentation.space

import androidx.lifecycle.viewModelScope
import com.umc.edison.domain.usecase.bubble.GetKeywordBubbleUsecase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.remote.model.space.GetKeywordBubbleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
@HiltViewModel
class KeywordViewModel @Inject constructor(
    toastManager: ToastManager,
    getKeywordBubbleUsecase: GetKeywordBubbleUsecase
): BaseViewModel(toastManager){


    private val _uiState = MutableStateFlow(KeywordMapState.DEFAULT)
    val uiState = _uiState.asStateFlow()

}

