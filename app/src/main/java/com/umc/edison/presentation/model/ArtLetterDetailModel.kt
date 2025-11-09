package com.umc.edison.presentation.model

import com.umc.edison.domain.model.artLetter.ArtLetter

data class ArtLetterDetailModel(
    val artLetterId: Int,
    val title: String,
    val content: String,
    val category: String,
    val readTime: Int,
    val writerSummary: WriterSummaryModel,
    val tags: List<String>,
    val thumbnail: String,
    val likesCnt: Int,
    val liked: Boolean,
    val scraped: Boolean,
) {
    companion object {
        val DEFAULT = ArtLetterDetailModel(
            artLetterId = 0,
            title = "",
            content = "",
            category = "",
            readTime = 0,
            writerSummary = WriterSummaryModel.DEFAULT,
            tags = emptyList(),
            thumbnail = "",
            likesCnt = 0,
            liked = false,
            scraped = false,
        )
    }
}

fun ArtLetter.toDetailPresentation(): ArtLetterDetailModel = ArtLetterDetailModel(
    artLetterId = artLetterId,
    title = title,
    content = content,
    category = category,
    readTime = readTime,
    writerSummary = writerSummary.toPresentation(),
    tags = tags,
    thumbnail = thumbnail,
    likesCnt = likesCnt,
    liked = liked,
    scraped = scraped
)
