package com.umc.edison.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.usecase.user.GetLogInStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getLogInStateUseCase: GetLogInStateUseCase
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    fun checkLogin() {
        viewModelScope.launch {
            getLogInStateUseCase().collect { res ->
                Log.d("SplashVM", "checkLogin: $res")

                when (res) {
                    is DataResource.Success -> _isLoggedIn.value = res.data
                    is DataResource.Error   -> _isLoggedIn.value = false
                    is DataResource.Loading -> { /* 필요시 로딩 처리 */ }
                }
            }
        }
    }
}
