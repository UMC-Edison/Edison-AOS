package com.umc.edison.domain.repository

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.s3.PresignedUrl
import kotlinx.coroutines.flow.Flow
import java.io.File

interface S3Repository {
     // 업로드용: 파일명으로 서버가 key 생성 + presigned PUT URL 발급
     fun getUploadPresignedUrl(fileName: String): Flow<DataResource<PresignedUrl>>

     // presigned PUT URL로 실제 업로드 진행
     fun uploadImageToS3(presignedUrl: String, file: File): Flow<DataResource<Unit>>

     // 조회용: 저장된 key로 presigned GET URL 발급
     fun getDownloadPresignedUrl(key: String): Flow<DataResource<PresignedUrl>>

}