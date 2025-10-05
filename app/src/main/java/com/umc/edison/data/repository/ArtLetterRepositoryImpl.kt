package com.umc.edison.data.repository

import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.ArtLetterRemoteDataSource
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.artLetter.ArtLetter
import com.umc.edison.domain.model.artLetter.ArtLetterKeyWord
import com.umc.edison.domain.repository.ArtLetterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArtLetterRepositoryImpl @Inject constructor(
    private val artLetterRemoteDataSource: ArtLetterRemoteDataSource,
    private val resourceFactory: FlowBoundResourceFactory
) : ArtLetterRepository {
    // READ
    override fun getAllArtLetterCategories(): Flow<DataResource<List<String>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getAllArtLetterCategories() }

    override fun getAllArtLetters(): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getAllArtLetters() }

    override fun getAllEditorPickArtLetters(): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getAllEditorPickArtLetters() }

    override fun getSearchMoreArtLetters(): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getSearchMoreArtLetters() }

    override fun getMoreArtLetters(id: Int): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getMoreArtLetters(id) }

    override fun getAllRecommendArtLetterKeyWords(): Flow<DataResource<List<ArtLetterKeyWord>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getAllRecommendArtLetterKeyWords() }

    override fun getAllScrappedArtLetters(): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getAllScrappedArtLetters() }

    override fun getArtLetter(id: Int): Flow<DataResource<ArtLetter>> =
        resourceFactory.remote { artLetterRemoteDataSource.getArtLetter(id) }

    override fun getScrappedArtLettersByCategory(category: String): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getScrappedArtLettersByCategory(category) }

    override fun getSortedArtLetters(sortBy: String): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getSortedArtLetters(sortBy) }

    override fun searchArtLetters(keyword: String, sortType: String): Flow<DataResource<List<ArtLetter>>> =
        resourceFactory.remote { artLetterRemoteDataSource.getSearchArtLetterResults(keyword, sortType) }

    // UPDATE
    override fun likeArtLetter(id: Int): Flow<DataResource<Unit>> =
        resourceFactory.remote { artLetterRemoteDataSource.postArtLetterLike(id) }

    override fun scrapArtLetter(id: Int): Flow<DataResource<Unit>> =
        resourceFactory.remote { artLetterRemoteDataSource.postArtLetterScrap(id) }
}
