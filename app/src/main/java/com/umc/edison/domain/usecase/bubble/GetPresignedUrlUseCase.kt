package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.repository.S3Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresignedUrlUseCase@Inject constructor(
    private val s3Repository: S3Repository
) {

    operator fun invoke(fileName: String): Flow<DataResource<String>> {
        return s3Repository.getPresignedUrl(fileName)
    }
}