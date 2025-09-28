package com.umc.edison.presentation.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.edison.domain.DataResource
import com.umc.edison.presentation.ToastManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

open class BaseViewModel @Inject constructor(
    private val toastManager: ToastManager,
) : ViewModel() {
    internal val _baseState = MutableStateFlow(BaseState.DEFAULT)
    val baseState = _baseState.asStateFlow()

    /**
     * 공통적으로 DataResource를 처리하는 함수
     */
    protected fun <T> collectDataResource(
        flow: Flow<DataResource<T>>,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        onLoading: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            flow.onCompletion {
                Log.d("collectDataResource", "onComplete")
                _baseState.update {
                    it.copy(isLoading = false)
                }
                onComplete?.invoke()
            }.collect { dataResource ->
                when (dataResource) {
                    is DataResource.Success -> {
                        Log.d("collectDataResource", "onSuccess: ${dataResource.data}")
                        onSuccess(dataResource.data)
                    }

                    is DataResource.Error -> {
                        Log.e("collectDataResource", "onError: ${dataResource.throwable}")
                        _baseState.update {
                            it.copy(error = dataResource.throwable)
                        }
                        onError?.invoke(dataResource.throwable)
                    }

                    is DataResource.Loading -> {
                        Log.d("collectDataResource", "onLoading")
                        _baseState.update {
                            it.copy(isLoading = true)
                        }
                        onLoading?.invoke()
                    }
                }
            }
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            toastManager.showToast(message)
        }
    }
}
