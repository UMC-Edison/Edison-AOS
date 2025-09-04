package com.umc.edison.remote.api

import com.umc.edison.remote.model.BaseResponse
import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.ResponseWithListData
import com.umc.edison.remote.model.ResponseWithPagination
import com.umc.edison.remote.model.artletter.GetAllArtLettersResponse
import com.umc.edison.remote.model.artletter.GetArtLetterCategoryResponse
import com.umc.edison.remote.model.artletter.GetArtLetterDetailResponse
import com.umc.edison.remote.model.artletter.GetArtLetterKeywordResponse
import com.umc.edison.remote.model.artletter.GetSortedArtLettersResponse
import com.umc.edison.remote.model.artletter.PostArtLetterLikeResponse
import com.umc.edison.remote.model.artletter.PostArtLetterScrapResponse
import com.umc.edison.remote.model.artletter.GetEditorPickResponse
import com.umc.edison.remote.model.artletter.GetMoreArtLettersResponse
import com.umc.edison.remote.model.artletter.GetRecentSearchesResponse
import com.umc.edison.remote.model.artletter.GetSearchArtLettersResponse
import com.umc.edison.remote.model.mypage.GetMyScrapArtLettersResponse
import com.umc.edison.remote.model.mypage.GetScrapArtLettersByCategoryResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtLetterApiService {
    @GET("/artletters")
    suspend fun getAllArtLetters(): ResponseWithPagination<GetAllArtLettersResponse>

    @GET("/artletters")
    suspend fun getSortedArtLetters(@Query("sortType") sortBy: String): ResponseWithListData<GetSortedArtLettersResponse>

    @GET("/artletters/editor-pick")
    suspend fun getEditorPick(): ResponseWithListData<GetEditorPickResponse>

    @GET("/artletters/more/{currentId}")
    suspend fun getMoreArtLetters(@Path("currentId") currentId: Int): ResponseWithListData<GetMoreArtLettersResponse>

    @GET("/artletters/{letterId}")
    suspend fun getArtLetterDetail(@Path("letterId") letterId: Int): ResponseWithData<GetArtLetterDetailResponse>

    @GET("/artletters/recommend-bar/keyword")
    suspend fun getRecommendedKeywords(): ResponseWithListData<GetArtLetterKeywordResponse>

    @GET("/artletters/recommend-bar/category")
    suspend fun getRecommendedCategories(): ResponseWithData<GetArtLetterCategoryResponse>

    @GET("/artletters/search")
    suspend fun getSearchArtLetters(@Query("keyword") keyword: String, @Query("sortType") sortType: String): ResponseWithPagination<GetSearchArtLettersResponse>

    @GET("artletters/scrap")
    suspend fun getMyScrapArtLetters(): ResponseWithPagination<GetMyScrapArtLettersResponse>

    @GET("artletters/scrap/{category}")
    suspend fun getScrappedArtLettersByCategory(@Path("category") category: String): ResponseWithPagination<GetScrapArtLettersByCategoryResponse>

    @GET("/artletters/search-memory")
    suspend fun getRecentSearches(): ResponseWithData<GetRecentSearchesResponse>

    @POST("/artletters/{artletterId}/scrap")
    suspend fun postArtLetterScrap(@Path("artletterId") id: Int): ResponseWithData<PostArtLetterScrapResponse>

    @POST("/artletters/{artletterId}/like")
    suspend fun postArtLetterLike(@Path("artletterId") id: Int): ResponseWithData<PostArtLetterLikeResponse>

    @DELETE("/artletters/search-memory")
    suspend fun removeRecentSearch(@Query("keyword") keyword: String): BaseResponse
}