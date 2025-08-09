package com.umc.edison.remote.api

import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.s3.UrlResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface S3ApiService {

    @POST("s3/upload-url")
    suspend fun getPresignedUrl(
        @Query("fileName") fileName: String
    ): ResponseWithData<UrlResponse>


    @GET("s3/get-img")
    suspend fun getDownloadLink(
        @Query("key") key: String
    ): ResponseWithData<String>
}