package com.umc.edison.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.ui.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MakeNickNameViewModel @Inject constructor(
    toastManager: ToastManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(toastManager) {

    private val idToken: String = savedStateHandle.get<String>("idToken") ?: ""

    private val _uiState = MutableStateFlow(MakeNickNameState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    fun onNicknameChange(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun makeNickName(
        navController: NavHostController,
    ) {
        val nickname = uiState.value.nickname

        if (nickname.isBlank()) {
            showToast("닉네임을 입력해주세요.")
            return
        }

        if (idToken.isBlank()) {
            showToast("로그인 정보가 없습니다. 다시 시도해주세요.")
            return
        }

        navController.navigate(
            NavRoute.IdentityTest.createRoute(
                idToken = idToken,
                nickname = nickname
            )
        ) {
            popUpTo(NavRoute.MakeNickName.route) { inclusive = true }
        }
    }
}
