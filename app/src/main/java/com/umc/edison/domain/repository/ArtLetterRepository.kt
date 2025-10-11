package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.artLetter.ArtLetter
import com.umc.edison.domain.model.artLetter.ArtLetterKeyWord
import kotlinx.coroutines.flow.Flow

interface ArtLetterRepository {
    // READ
    fun getAllArtLetterCategories(): Flow<DataResource<List<String>>>
    fun getAllArtLetters(): Flow<DataResource<List<ArtLetter>>>
    fun getAllEditorPickArtLetters(): Flow<DataResource<List<ArtLetter>>>
    fun getSearchMoreArtLetters(): Flow<DataResource<List<ArtLetter>>>
    fun getMoreArtLetters(id: Int): Flow<DataResource<List<ArtLetter>>>
    fun getAllRecommendArtLetterKeyWords(): Flow<DataResource<List<ArtLetterKeyWord>>>
    fun getAllScrappedArtLetters(): Flow<DataResource<List<ArtLetter>>>
    fun getArtLetter(id: Int): Flow<DataResource<ArtLetter>>
    fun getScrappedArtLettersByCategory(category: String): Flow<DataResource<List<ArtLetter>>>
    fun getSortedArtLetters(sortBy: String): Flow<DataResource<List<ArtLetter>>>
    fun searchArtLetters(keyword: String, sortType: String): Flow<DataResource<List<ArtLetter>>>

    // UPDATE
    fun likeArtLetter(id: Int): Flow<DataResource<Unit>>
    fun scrapArtLetter(id: Int): Flow<DataResource<Unit>>
}
