package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.ArtLetterRemoteDataSource
import com.umc.edison.data.model.artLetter.ArtLetterEntity
import com.umc.edison.data.model.artLetter.ArtLetterKeyWordEntity
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity
import com.umc.edison.remote.api.ArtLetterApiService
import com.umc.edison.remote.model.artletter.toData
import javax.inject.Inject

class ArtLetterRemoteDataSourceImpl @Inject constructor(
    private val artLetterApiService: ArtLetterApiService,
) : ArtLetterRemoteDataSource {
    // READ
    override suspend fun getAllArtLetterCategories(): List<String> {
        return artLetterApiService.getRecommendedCategories().data.categories
    }

    override suspend fun getAllArtLetters(): List<ArtLetterPreviewEntity> {
        val response = artLetterApiService.getAllArtLetters().data

        return response.map { it.toData() }
    }

    override suspend fun getAllEditorPickArtLetters(): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getEditorPick().data.map { it.toData() }
    }

    override suspend fun getSearchMoreArtLetters(): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getSearchMoreArtLetters().data.map { it.toData() }
    }

    override suspend fun getMoreArtLetters(id: Int): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getMoreArtLetters(id).data.map { it.toData() }
    }

    override suspend fun getAllRecommendArtLetterKeyWords(): List<ArtLetterKeyWordEntity> {
        val response = artLetterApiService.getRecommendedKeywords().data
        return response.map { it.toData() }
    }

    override suspend fun getAllScrappedArtLetters(): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getMyScrapArtLetters().data.map { it.toData() }
    }

    override suspend fun getArtLetter(id: Int): ArtLetterEntity {
        return artLetterApiService.getArtLetterDetail(id).data.toData()
    }

    override suspend fun getScrappedArtLettersByCategory(category: String): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getScrappedArtLettersByCategory(category).data.map { it.toData() }
    }

    override suspend fun getSortedArtLetters(sortBy: String): List<ArtLetterPreviewEntity> {
        return artLetterApiService.getSortedArtLetters(sortBy).data.toData()
    }

    override suspend fun getSearchArtLetterResults(keyword: String, sortType: String): List<ArtLetterPreviewEntity> {
        val response = artLetterApiService.getSearchArtLetters(keyword, sortType).data

        return response.map { it.toData() }
    }

    // UPDATE
    override suspend fun postArtLetterLike(id: Int) {
        artLetterApiService.postArtLetterLike(id)
    }

    override suspend fun postArtLetterScrap(id: Int) {
        artLetterApiService.postArtLetterScrap(id)
    }
}