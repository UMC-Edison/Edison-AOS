package com.umc.edison.remote.datasources

import com.umc.edison.data.datasources.S3RemoteDataSource
import com.umc.edison.data.model.s3.PresignedUrlEntity
import com.umc.edison.remote.api.S3ApiService
import javax.inject.Inject

class S3RemoteDataSourceImpl @Inject constructor(
    private val s3ApiService: S3ApiService

):S3RemoteDataSource{


    override suspend fun getUploadPresignedUrl(fileName: String): PresignedUrlEntity {
        val res = s3ApiService.getPresignedUrl(fileName)
        return PresignedUrlEntity(
            key = res.key,
            presignedUrl = res.presignedUrl
        )
    }

    override suspend fun getDownloadPresignedUrl(key: String): PresignedUrlEntity {
        val url = s3ApiService.getDownloadLink(key)
        return PresignedUrlEntity(
            key = key,
            presignedUrl = url
        )
    }


}