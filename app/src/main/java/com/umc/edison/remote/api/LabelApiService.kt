package com.umc.edison.remote.api

import com.umc.edison.remote.model.ResponseWithData
import com.umc.edison.remote.model.label.GetAllLabelsResponse
import retrofit2.http.GET

interface LabelApiService {
    @GET("/labels")
    suspend fun getAllLabels(): ResponseWithData<List<GetAllLabelsResponse>>
}
