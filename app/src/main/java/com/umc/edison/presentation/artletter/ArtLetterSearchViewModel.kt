package com.umc.edison.presentation.artletter

import com.umc.edison.domain.usecase.artletter.GetAllArtLetterCategoriesUseCase
import com.umc.edison.domain.usecase.artletter.GetAllRecommendArtLetterKeyWordsUseCase
import com.umc.edison.domain.usecase.artletter.GetSearchMoreArtLettersUseCase
import com.umc.edison.domain.usecase.recentSearch.GetAllRecentSearchesUseCase
import com.umc.edison.domain.usecase.artletter.SearchArtLettersUseCase
import com.umc.edison.domain.usecase.recentSearch.DeleteRecentSearchUseCase
import com.umc.edison.domain.usecase.artletter.ScrapArtLetterUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.model.toPreviewPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ArtLetterSearchViewModel @Inject constructor(
    toastManager: ToastManager,
    private val searchArtLettersUseCase: SearchArtLettersUseCase,
    private val getSearchMoreArtLettersUseCase: GetSearchMoreArtLettersUseCase,
    private val scrapArtLetterUseCase: ScrapArtLetterUseCase,
    private val getAllRecommendArtLetterKeyWordsUseCase: GetAllRecommendArtLetterKeyWordsUseCase,
    private val deleteRecentSearchUseCase: DeleteRecentSearchUseCase,
    private val getAllRecentSearchesUseCase: GetAllRecentSearchesUseCase,
    private val getAllArtLetterCategoriesUseCase: GetAllArtLetterCategoriesUseCase
) : BaseViewModel(toastManager) {
    private val _uiState = MutableStateFlow(ArtLetterSearchState.DEFAULT)
    val uiState = _uiState.asStateFlow()

    init {
        fetchRecommendedArtLetters()
        getRecentSearches()
        getKeyWords()
        getCategories()
    }

    fun searchArtLetters() {
        if (_uiState.value.query.isEmpty()) {
            return
        }

        if (uiState.value.query.trim().isEmpty()) {
            return
        }

        collectDataResource(
            flow = searchArtLettersUseCase(_uiState.value.query),
            onSuccess = { artLetters ->
                _uiState.update {
                    it.copy(
                        artLetters = artLetters.toPreviewPresentation(),
                        lastQuery = it.query,
                        isSearchActivated = true
                    )
                }
            },
            onComplete = {
                getRecentSearches()
            }
        )
    }

    fun searchArtLetters(query: String) {
        _uiState.update {
            it.copy(query = query)
        }

        searchArtLetters()
    }

    fun getSortedArtLetters(sortType: String) {
        collectDataResource(
            flow = searchArtLettersUseCase(_uiState.value.query, sortType),
            onSuccess = { artLetters ->
                _uiState.update { it.copy(artLetters = artLetters.toPreviewPresentation()) }
            },
        )
    }

    private fun getRecentSearches() {
        collectDataResource(
            flow = getAllRecentSearchesUseCase(),
            onSuccess = { recentSearches ->
                _uiState.update { it.copy(recentSearches = recentSearches) }
            },
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(query = query)
        }
    }

    fun removeSearchHistory(keyword: String) {
        collectDataResource(
            flow = deleteRecentSearchUseCase(keyword),
            onSuccess = {
                _uiState.update {
                    it.copy(
                        recentSearches = it.recentSearches.filter { search -> search != keyword }
                    )
                }
            },
        )
    }

    private fun fetchRecommendedArtLetters() {
        collectDataResource(
            flow = getSearchMoreArtLettersUseCase(),
            onSuccess = { artLetters ->
                _uiState.update { it.copy(recommendedArtLetters = artLetters.toPreviewPresentation()) }
            },
        )
    }

    fun postArtLetterScrap(id: Int) {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginModal = true) }
            return
        }

        collectDataResource(
            flow = scrapArtLetterUseCase(id),
            onSuccess = {
                _uiState.update {
                    it.copy(
                        artLetters = it.artLetters.map { artLetter ->
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

    fun getKeyWords() {
        collectDataResource(
            flow = getAllRecommendArtLetterKeyWordsUseCase(),
            onSuccess = { keywords ->
                _uiState.update { it.copy(keywords = keywords.toPresentation()) }
            },
        )
    }

    fun getCategories() {
        collectDataResource(
            flow = getAllArtLetterCategoriesUseCase(),
            onSuccess = { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories
                    )
                }
            },
        )
    }

    fun updateShowLoginModal(show: Boolean) {
        _uiState.update { it.copy(showLoginModal = show) }
    }
}
