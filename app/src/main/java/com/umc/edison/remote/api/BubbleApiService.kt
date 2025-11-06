package com.umc.edison.remote.api

import com.umc.edison.remote.model.ResponseWithPagination
import com.umc.edison.remote.model.bubble.BubbleResponse
import retrofit2.http.GET

interface BubbleApiService {
    @GET("/bubbles")
    suspend fun getAllBubbles(): ResponseWithPagination<BubbleResponse>
}
