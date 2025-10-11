package com.umc.edison.remote.model.artletter

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetSortedArtLettersResponse(
    @SerializedName("artletterId") val artLetterId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("likesCnt") val likesCnt: Int,
    @SerializedName("scrapsCnt") val scrapsCnt: Int,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("scraped") val scraped: Boolean,
) : RemoteMapper<ArtLetterPreviewEntity> {
    override fun toData(): ArtLetterPreviewEntity = ArtLetterPreviewEntity(
        artLetterId = artLetterId,
        title = title,
        thumbnail = thumbnail,
        scraped = scraped,
        tags = emptyList()
    )
}

fun List<GetSortedArtLettersResponse>.toData(): List<ArtLetterPreviewEntity> = map { it.toData() }
