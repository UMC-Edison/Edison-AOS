package com.umc.edison.domain.usecase.bubble

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.repository.S3Repository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UploadImagesToS3UseCase @Inject constructor(
    private val s3Repository: S3Repository
) {

    operator fun invoke(presignedUrl: String, file: File) {
        return s3Repository.uploadImageToS3(presignedUrl,file)
    }
}
