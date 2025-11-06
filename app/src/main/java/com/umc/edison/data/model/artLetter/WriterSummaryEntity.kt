package com.umc.edison.data.model.artLetter

import com.umc.edison.data.model.DataMapper
import com.umc.edison.domain.model.artLetter.WriterSummary

data class WriterSummaryEntity(
    val writerId: Int,
    val writerName: String,
    val writerUrl: String?
) : DataMapper<WriterSummary> {
    override fun toDomain(): WriterSummary = WriterSummary(
        writerId,
        writerName,
        writerUrl,
    )
}
