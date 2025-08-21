package com.umc.edison.remote.api

import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.bubble.BubbleResponse
import com.umc.edison.remote.model.space.GetBubblePositionResponse
import com.umc.edison.remote.model.space.GetKeywordBubbleResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BubbleSpaceApiService {
    @GET("/spaces/map")
    suspend fun getBubblePosition(): ResponseWithData<List<GetBubblePositionResponse>>

    @POST("/spaces/similarity")
    suspend fun getKeywordBubbles(@Query("keyword") keyword: String): ResponseWithData<List<GetKeywordBubbleResponse>>
}