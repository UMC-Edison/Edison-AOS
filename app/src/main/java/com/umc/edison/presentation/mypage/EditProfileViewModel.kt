package com.umc.edison.presentation.mypage

import android.net.Uri
import com.umc.edison.domain.usecase.user.GetMyProfileInfoUseCase
import com.umc.edison.domain.usecase.user.UpdateProfileInfoUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    toastManager: ToastManager,
    private val getMyProfileInfoUseCase: GetMyProfileInfoUseCase,
    private val updateUserProfileUseCase: UpdateProfileInfoUseCase
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(EditProfileState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        fetchProfileInfo()
    }

    private fun fetchProfileInfo() {
        collectDataResource(
            flow = getMyProfileInfoUseCase(),
            onSuccess = { user ->
                _uiState.update { it.copy(user = user.toPresentation()) }
            },
        )
    }

    fun updateUserProfile() {
        collectDataResource(
            flow = updateUserProfileUseCase(_uiState.value.user.toDomain()),
            onSuccess = {
                showToast("프로필이 수정되었습니다.")
            },
        )
    }

    fun updateUserNickname(nickname: String) {
        _uiState.update { it.copy(user = it.user.copy(nickname = nickname)) }
    }

    fun updateUserProfileImage(image: Uri) {
        _uiState.update {
            it.copy(
                user = it.user.copy(profileImage = image.toString())
            )
        }
    }
}
