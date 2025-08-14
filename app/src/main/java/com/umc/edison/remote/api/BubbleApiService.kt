package com.umc.edison.remote.api

import com.umc.edison.remote.model.BaseResponse
import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.ResponseWithPagination
import com.umc.edison.remote.model.bubble.AddBubbleRequest
import com.umc.edison.remote.model.bubble.BubbleResponse
import com.umc.edison.remote.model.bubble.RecoverBubbleResponse
import com.umc.edison.remote.model.bubble.TrashBubbleResponse
import com.umc.edison.remote.model.bubble.UpdateBubbleRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BubbleApiService {
    @GET("/bubbles")
    suspend fun getAllBubbles(): ResponseWithPagination<BubbleResponse>
    @POST("/bubbles")
    suspend fun addBubble(@Body bubble: AddBubbleRequest): ResponseWithData<BubbleResponse>

    @PATCH("/bubbles/{bubbleId}/restore")
    suspend fun restoreBubble(@Path("bubbleId") id: String): ResponseWithData<RecoverBubbleResponse>

    @PATCH("/bubbles/{bubbleId}")
    suspend fun updateBubble(@Path("bubbleId") id: String, @Body bubble: UpdateBubbleRequest): ResponseWithData<BubbleResponse>

    @PATCH("/bubbles/{bubbleId}/delete")
    suspend fun trashBubble(@Path("bubbleId") id: String): ResponseWithData<TrashBubbleResponse>

    @DELETE("/bubbles/trashbin/{bubbleId}")
    suspend fun deleteBubble(@Path("bubbleId") id: String): BaseResponse
}