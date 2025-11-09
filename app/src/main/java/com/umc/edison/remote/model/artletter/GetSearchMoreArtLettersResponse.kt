package com.umc.edison.remote.model.artletter

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetSearchMoreArtLettersResponse(
    @SerializedName("artletterId") val artLetterId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("tags") val tags: String,
    @SerializedName("isScraped") val scraped: Boolean
) : RemoteMapper<ArtLetterPreviewEntity> {
    override fun toData(): ArtLetterPreviewEntity = ArtLetterPreviewEntity(
        artLetterId = artLetterId,
        title = title,
        tags = tags.split(" ").filter { it.isNotBlank() },
        thumbnail = thumbnail,
        scraped = scraped
    )
}
