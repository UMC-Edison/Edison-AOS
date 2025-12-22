package com.umc.edison.remote.api

import SignUpResponse
import com.umc.edison.remote.model.BaseResponse
import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.login.IdTokenRequest
import com.umc.edison.remote.model.login.SetIdentityKeywordRequest
import com.umc.edison.remote.model.login.LoginResponse
import com.umc.edison.remote.model.login.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    @POST("members/google/login")
    suspend fun googleLogin(@Body request: IdTokenRequest): ResponseWithData<LoginResponse>


    @POST("/members/identity")
    suspend fun setUserIdentityAndInterest(
        @Body request: SetIdentityKeywordRequest
    ): BaseResponse

    @POST("/members/google/signup")
    suspend fun googleSignup(@Body request: SignUpRequest): ResponseWithData<SignUpResponse>
}