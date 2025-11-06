package com.umc.edison.domain.model.artLetter

data class WriterSummary(
    val writerId: Int,
    val writerName: String,
    val writerUrl: String?
) {
    companion object {
        val DEFAULT = WriterSummary(
            writerId = 0,
            writerName = "",
            writerUrl = null
        )
    }
}
