package com.umc.edison.presentation.artletter

import androidx.lifecycle.SavedStateHandle
import com.umc.edison.domain.usecase.artletter.GetArtLetterUseCase
import com.umc.edison.domain.usecase.artletter.GetAllRandomArtLettersUseCase
import com.umc.edison.domain.usecase.artletter.LikeArtLetterUseCase
import com.umc.edison.domain.usecase.artletter.ScrapArtLetterUseCase
import com.umc.edison.domain.usecase.user.GetLogInStateUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toDetailPresentation
import com.umc.edison.presentation.model.toPreviewPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ArtLetterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    toastManager: ToastManager,
    private val getArtLetterUseCase: GetArtLetterUseCase,
    private val likeArtLetterUseCase: LikeArtLetterUseCase,
    private val scrapArtLetterUseCase: ScrapArtLetterUseCase,
    private val getAllRandomArtLettersUseCase: GetAllRandomArtLettersUseCase,
    private val getLogInStateUseCase: GetLogInStateUseCase
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(ArtLetterDetailState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        val id: Int = savedStateHandle["artLetterId"]
            ?: throw IllegalArgumentException("artLetterId is required")

        fetchArtLetterDetail(id)
        fetchRandomArtLetters()
        getLoginState()
    }

    private fun getLoginState() {
        collectDataResource(
            flow = getLogInStateUseCase(),
            onSuccess = { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            },
        )
    }

    private fun fetchArtLetterDetail(id: Int) {

        collectDataResource(
            flow = getArtLetterUseCase(id),
            onSuccess = { artLetterDetail ->
                _uiState.update {
                    it.copy(
                        artLetter = artLetterDetail.toDetailPresentation(),
                    )
                }
            },

        )
    }



    private fun fetchRandomArtLetters() {
        collectDataResource(
            flow = getAllRandomArtLettersUseCase(),
            onSuccess = { artLetters ->
                val unique = artLetters.filter {
                    it.artLetterId != _uiState.value.artLetter.artLetterId
                }
                _uiState.update {
                    it.copy(
                        relatedArtLetters = unique.toPreviewPresentation(),
                    )
                }
            },

        )
    }



    fun likeArtLetter() {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginModal = true) }
            return
        }

        val artLetter = _uiState.value.artLetter

        collectDataResource(
            flow = likeArtLetterUseCase(artLetter.artLetterId),
            onSuccess = {
                _uiState.update {
                    it.copy(
                        artLetter = artLetter.copy(
                            liked = !artLetter.liked,
                            likesCnt = if (artLetter.liked) artLetter.likesCnt - 1 else artLetter.likesCnt + 1
                        )
                    )
                }
            },
        )
    }

    fun scrapArtLetter() {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginModal = true) }
            return
        }

        val artLetter = _uiState.value.artLetter

        collectDataResource(
            flow = scrapArtLetterUseCase(artLetter.artLetterId),
            onSuccess = {
                _uiState.update {
                    it.copy(
                        artLetter = artLetter.copy(scraped = !artLetter.scraped)
                    )
                }
            },
        )
    }

    fun scrapArtLetter(id: Int) {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginModal = true) }
            return
        }

        if (id == _uiState.value.artLetter.artLetterId) {
            scrapArtLetter()
        } else {
            val relatedArtLetter = _uiState.value.relatedArtLetters.find { it.artLetterId == id }

            if (relatedArtLetter != null) {
                collectDataResource(
                    flow = scrapArtLetterUseCase(id),
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                relatedArtLetters = it.relatedArtLetters.map { artLetter ->
                                    if (artLetter.artLetterId == id) {
                                        artLetter.copy(scraped = !artLetter.scraped)
                                    } else {
                                        artLetter
                                    }
                                }
                            )
                        }
                    },
                )
            }
        }
    }

    fun updateShowLoginModal(show: Boolean) {
        _uiState.update { it.copy(showLoginModal = show) }
    }
}
