package com.umc.edison.remote.api

import com.umc.edison.remote.model.BaseResponse
import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.label.AddLabelRequest
import com.umc.edison.remote.model.label.GetAllLabelsResponse
import com.umc.edison.remote.model.label.LabelResponse
import com.umc.edison.remote.model.label.UpdateLabelRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LabelApiService {
    @GET("/labels")
    suspend fun getAllLabels(): ResponseWithData<List<GetAllLabelsResponse>>
    @POST("/labels")
    suspend fun addLabel(@Body label: AddLabelRequest): ResponseWithData<LabelResponse>

    @PATCH("/labels/{labelId}")
    suspend fun updateLabel(@Path("labelId") id: String, @Body label: UpdateLabelRequest): ResponseWithData<LabelResponse>

    @DELETE("/labels/{labelId}")
    suspend fun deleteLabel(@Path("labelId") id: String): BaseResponse
}