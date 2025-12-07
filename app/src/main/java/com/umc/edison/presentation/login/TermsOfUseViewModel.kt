package com.umc.edison.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import com.umc.edison.domain.usecase.user.GetMyProfileInfoUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.ui.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TermsOfUseViewModel @Inject constructor(
    toastManager: ToastManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(TermsOfUseState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        val fromSignUp: Boolean = savedStateHandle.get<Boolean>("fromSignUp") ?: false
        val idToken: String = savedStateHandle.get<String>("idToken") ?: ""

        _uiState.update {
            it.copy(
                fromSignUp = fromSignUp,
                idToken = idToken
            )
        }
    }


    fun buttonClicked(navController: NavHostController) {
        val state = uiState.value

        if (state.fromSignUp) {
            navController.navigate(
                NavRoute.MakeNickName.createRoute(
                    idToken = state.idToken
                )
            ) {
                popUpTo(NavRoute.TermsOfUse.route) { inclusive = true }
            }
        }  else {
            navController.navigate(NavRoute.MyEdison.route) {
                popUpTo(NavRoute.TermsOfUse.route) { inclusive = true }
            }
        }
    }
}
