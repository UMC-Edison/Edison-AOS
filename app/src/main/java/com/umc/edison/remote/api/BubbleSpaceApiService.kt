package com.umc.edison.remote.api

import com.umc.edison.remote.model.BubbleRequest
import com.umc.edison.remote.model.BubbleResponse
import com.umc.edison.remote.model.Response
import com.umc.edison.remote.model.ResponseWithData
import retrofit2.http.GET
import retrofit2.http.POST

interface BubbleSpaceApiService {

    // TODO: 사용 예시로 추후 버블 스페이스 관련 명세가 완료되면 수정 필요
    @GET("bubbles")
    suspend fun getAllBubbles(): ResponseWithData<List<BubbleResponse>>

    @POST("bubbles")
    suspend fun syncBubbles(bubbles: List<BubbleRequest>): Response
}