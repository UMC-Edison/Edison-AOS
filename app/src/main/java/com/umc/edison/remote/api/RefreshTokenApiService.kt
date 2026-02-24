package com.umc.edison.remote.api

import com.umc.edison.remote.model.RefreshTokenResponse
import com.umc.edison.remote.model.ResponseWithData
import retrofit2.http.Header
import retrofit2.http.POST

interface RefreshTokenApiService {
    @POST("members/refresh")
    suspend fun refreshToken(@Header("Refresh-Token") refreshToken: String): ResponseWithData<RefreshTokenResponse>
}