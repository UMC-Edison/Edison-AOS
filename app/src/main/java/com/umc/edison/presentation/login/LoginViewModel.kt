package com.umc.edison.presentation.login

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.ui.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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
        googleLoginHelper.signInWithGoogle(context) { state ->
            handleLoginState(state, navController)
        }
    }

    private fun handleLoginState(state: GoogleLoginState, navController: NavHostController) {
        when (state) {
            is GoogleLoginState.Loading -> {
                _baseState.update { it.copy(isLoading = true) }
            }

            is GoogleLoginState.Success -> {
                _baseState.update { it.copy(isLoading = false) }
                _uiState.update { it.copy(user = state.userModel) }

                showToast("로그인 성공!")

                navController.navigate(NavRoute.MyEdison.route) {
                    popUpTo(NavRoute.Login.route) { inclusive = true }
                }
            }

            is GoogleLoginState.MemberNotFound -> {
                _baseState.update { it.copy(isLoading = false) }
                _uiState.update { it.copy(pendingGoogleIdToken = state.idToken) }

                navController.navigate(
                    NavRoute.TermsOfUse.createRoute(
                        fromSignUp = true,
                        idToken = state.idToken
                    )
                ) {
                    popUpTo(NavRoute.Login.route) { inclusive = true }
                }
            }

            is GoogleLoginState.Failure -> {
                _baseState.update { it.copy(isLoading = false) }
                showToast(state.message)
            }

            GoogleLoginState.Idle -> {
                _baseState.update { it.copy(isLoading = false) }
            }
        }
    }
}