package com.umc.edison.remote.model.mypage

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetScrapArtLettersByCategoryResponse(
    @SerializedName("artletterId") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("likesCnt") val likesCnt: Int,
    @SerializedName("scrapsCnt") val scrapsCnt: Int,
    @SerializedName("scrappedAt") val date: String,
) : RemoteMapper<ArtLetterPreviewEntity> {
    override fun toData(): ArtLetterPreviewEntity = ArtLetterPreviewEntity(
        artLetterId = id,
        title = title,
        thumbnail = thumbnail,
        scraped = true,
        tags = emptyList()
    )
}