package com.umc.edison.data.repository

import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.repository.S3Repository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class S3RepositoryImpl @Inject constructor(
    private val bubbleRemoteDataSource: BubbleRemoteDataSource,
    private val resourceFactory: FlowBoundResourceFactory,
):S3Repository{

    override fun getPresignedUrl(fileName: String): Flow<DataResource<String>> =resourceFactory.remote(
        dataAction = {
            bubbleRemoteDataSource.getPresignedUrl(fileName)
        }

    )

    override fun uploadImageToS3(presignedUrl: String, file: File) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(presignedUrl)
            .put(file.asRequestBody("application/octet-stream".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("S3 업로드 실패: ${response.code} ${response.message}")
            }
        }
    }

    override fun getDownloadLink(key: String): Flow<DataResource<String>> =resourceFactory.remote(
        dataAction = {
            bubbleRemoteDataSource.getDownloadLink(key)
        }

    )
}