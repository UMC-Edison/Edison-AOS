package com.umc.edison.data.model.artLetter

import com.umc.edison.data.model.DataMapper
import com.umc.edison.domain.model.artLetter.ArtLetter

data class ArtLetterEntity(
    val artLetterId: Int,
    val title: String,
    val content: String,
    val category: String,
    val readTime: Int,
    val writerSummary: WriterSummaryEntity,
    val tags: List<String>,
    val thumbnail: String,
    val likesCnt: Int,
    val liked: Boolean,
    val scraped: Boolean
) : DataMapper<ArtLetter> {
    override fun toDomain(): ArtLetter = ArtLetter(
        artLetterId = artLetterId,
        title = title,
        content = content,
        category = category,
        readTime = readTime,
        writerSummary = writerSummary.toDomain(),
        tags = tags,
        thumbnail = thumbnail,
        likesCnt = likesCnt,
        liked = liked,
        scraped = scraped
    )
}
