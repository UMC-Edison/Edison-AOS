package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import kotlinx.coroutines.flow.Flow
import java.io.File

interface S3Repository {
     fun getPresignedUrl(fileName: String): Flow<DataResource<String>>
     fun uploadImageToS3(presignedUrl: String, file: File)
     fun getDownloadLink(key: String): Flow<DataResource<String>>
}