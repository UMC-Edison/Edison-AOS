package com.umc.edison.remote.model.artletter

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterEntity
import com.umc.edison.data.model.artLetter.WriterSummaryEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetArtLetterDetailResponse(
    @SerializedName("artletterId") val artLetterId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("readTime") val readTime: Int,
    @SerializedName("writerSummary") val writerSummary: WriterSummaryResponse,
    @SerializedName("tags") val tags: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("likesCnt") val likesCnt: Int,
    @SerializedName("scrapsCnt") val scrapsCnt: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("scraped") val scraped: Boolean
) : RemoteMapper<ArtLetterEntity> {
    override fun toData(): ArtLetterEntity = ArtLetterEntity(
        artLetterId = artLetterId,
        title = title,
        content = content,
        category = category,
        readTime = readTime,
        writerSummary = writerSummary.toData(),
        tags = tags.split(" "),
        thumbnail = thumbnail,
        likesCnt = likesCnt,
        liked = liked,
        scraped = scraped
    )

    data class WriterSummaryResponse(
        @SerializedName("writerId") val writerId: Int,
        @SerializedName("writerName") val writerName: String,
        @SerializedName("writerUrl") val writerUrl: String?
    ) : RemoteMapper<WriterSummaryEntity> {
        override fun toData(): WriterSummaryEntity = WriterSummaryEntity(
            writerId = writerId,
            writerName = writerName,
            writerUrl = writerUrl
        )
    }
}
