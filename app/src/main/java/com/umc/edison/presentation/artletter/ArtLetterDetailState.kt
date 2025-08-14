package com.umc.edison.presentation.artletter

import com.umc.edison.presentation.base.BaseState
import com.umc.edison.presentation.model.ArtLetterDetailModel
import com.umc.edison.presentation.model.ArtLetterPreviewModel

data class ArtLetterDetailState(
    val artLetter: ArtLetterDetailModel,
    val relatedArtLetters: List<ArtLetterPreviewModel>,
    val isLoggedIn: Boolean,
    val showLoginModal: Boolean,
) {
    companion object {
        val DEFAULT = ArtLetterDetailState(
            artLetter = ArtLetterDetailModel.DEFAULT,
            relatedArtLetters = emptyList(),
            isLoggedIn = false,
            showLoginModal = false,
        )
    }
}
