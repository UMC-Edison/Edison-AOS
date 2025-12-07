package com.umc.edison.presentation.login

import android.content.Context
import androidx.navigation.NavHostController
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.ui.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    toastManager: ToastManager,
    private val googleLoginHelper: GoogleLoginHelper
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(LoginState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context, navController: NavHostController) {
        googleLoginHelper.signInWithGoogle(
            context = context,
            onSuccess = { user ->
                CoroutineScope(Dispatchers.Main).launch {
                    showToast("로그인 성공!")
                    _uiState.update { it.copy(user = user) }
                    navController.navigate(NavRoute.MyEdison.route)
                }
            },
            onMemberNotFound = { idToken ->
                CoroutineScope(Dispatchers.Main).launch {
                    _uiState.update { it.copy(pendingGoogleIdToken = idToken) }

                    navController.navigate(NavRoute.TermsOfUse.createRoute(fromSignUp = true, idToken = idToken )) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                }
            },
            onFailure = {
                showToast("로그인 중 오류가 발생했습니다.")
            }
            ,
            onLoading = { isLoading ->
                _baseState.update { it.copy(isLoading = isLoading) }
            }
        )
    }
}
