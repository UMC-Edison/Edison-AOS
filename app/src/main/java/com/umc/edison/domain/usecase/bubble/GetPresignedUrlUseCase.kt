package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.s3.PresignedUrl
import com.umc.edison.domain.repository.S3Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresignedUrlUseCase @Inject constructor(
    private val s3Repository: S3Repository
) {
    operator fun invoke(fileName: String): Flow<DataResource<PresignedUrl>> {
        return s3Repository.getUploadPresignedUrl(fileName)
    }
}