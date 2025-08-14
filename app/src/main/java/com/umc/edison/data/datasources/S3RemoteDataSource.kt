package com.umc.edison.data.datasources

import com.umc.edison.data.model.s3.PresignedUrlEntity

interface S3RemoteDataSource {

    suspend fun getUploadPresignedUrl(fileName: String): PresignedUrlEntity

    suspend fun getDownloadPresignedUrl(key: String): PresignedUrlEntity
}