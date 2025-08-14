package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.s3.PresignedUrl
import kotlinx.coroutines.flow.Flow
import java.io.File

interface S3Repository {
     fun getUploadPresignedUrl(fileName: String): Flow<DataResource<PresignedUrl>>

     fun uploadImageToS3(presignedUrl: String, file: File): Flow<DataResource<Unit>>

     fun getDownloadPresignedUrl(key: String): Flow<DataResource<PresignedUrl>>

}