package com.umc.edison.presentation.model

import com.umc.edison.domain.model.artLetter.WriterSummary

data class WriterSummaryModel(
    val writerId: Int,
    val writerName: String,
    val writerUrl: String?
) {
    companion object {
        val DEFAULT = WriterSummaryModel(
            writerId = 0,
            writerName = "",
            writerUrl = null
        )
    }
}

fun WriterSummary.toPresentation(): WriterSummaryModel =
    WriterSummaryModel(
        writerId = writerId,
        writerName = writerName,
        writerUrl = writerUrl
    )
