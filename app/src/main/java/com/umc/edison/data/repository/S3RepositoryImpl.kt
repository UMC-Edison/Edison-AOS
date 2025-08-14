package com.umc.edison.data.repository

import com.umc.edison.data.bound.FlowBoundResourceFactory
import com.umc.edison.data.datasources.BubbleRemoteDataSource
import com.umc.edison.data.datasources.S3RemoteDataSource
import com.umc.edison.data.datasources.UserRemoteDataSource
import com.umc.edison.data.model.toDomain
import com.umc.edison.data.model.user.UserWithTokenEntity
import com.umc.edison.data.token.TokenManager
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.s3.PresignedUrl
import com.umc.edison.domain.model.user.User
import com.umc.edison.domain.repository.S3Repository
import com.umc.edison.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject


class S3RepositoryImpl @Inject constructor(
    private val s3RemoteDataSource: S3RemoteDataSource,
    private val resourceFactory: FlowBoundResourceFactory,
) : S3Repository {

    override fun getUploadPresignedUrl(fileName: String): Flow<DataResource<PresignedUrl>> =
        resourceFactory.remote(
            dataAction = {
                s3RemoteDataSource.getUploadPresignedUrl(fileName)
            }
        )

    override fun uploadImageToS3(presignedUrl: String, file: File): Flow<DataResource<Unit>> =
        resourceFactory.remote(
            dataAction = {
                withContext(Dispatchers.IO) {
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
            }
        )

    override fun getDownloadPresignedUrl(key: String): Flow<DataResource<PresignedUrl>> =
        resourceFactory.remote(
            dataAction = {
                s3RemoteDataSource.getDownloadPresignedUrl(key)
            }
        )
}
