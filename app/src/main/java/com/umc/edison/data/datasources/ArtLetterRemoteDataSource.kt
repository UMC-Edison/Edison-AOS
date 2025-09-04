package com.umc.edison.data.datasources

import com.umc.edison.data.model.artLetter.ArtLetterEntity
import com.umc.edison.data.model.artLetter.ArtLetterKeyWordEntity
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity

interface ArtLetterRemoteDataSource {
    // READ
    suspend fun getAllArtLetterCategories(): List<String>
    suspend fun getAllArtLetters(): List<ArtLetterPreviewEntity>
    suspend fun getAllEditorPickArtLetters(): List<ArtLetterPreviewEntity>
    suspend fun getAllRandomArtLetters(): List<ArtLetterPreviewEntity>
    suspend fun getMoreArtLetters(id: Int): List<ArtLetterPreviewEntity>
    suspend fun getAllRecommendArtLetterKeyWords(): List<ArtLetterKeyWordEntity>
    suspend fun getAllScrappedArtLetters(): List<ArtLetterPreviewEntity>
    suspend fun getArtLetter(id: Int): ArtLetterEntity
    suspend fun getScrappedArtLettersByCategory(category: String): List<ArtLetterPreviewEntity>
    suspend fun getSortedArtLetters(sortBy: String): List<ArtLetterPreviewEntity>
    suspend fun getSearchArtLetterResults(keyword: String, sortType: String): List<ArtLetterPreviewEntity>

    // UPDATE
    suspend fun postArtLetterLike(id: Int)
    suspend fun postArtLetterScrap(id: Int)
}