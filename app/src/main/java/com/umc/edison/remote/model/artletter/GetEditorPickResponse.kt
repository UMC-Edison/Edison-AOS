package com.umc.edison.remote.model.artletter

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterPreviewEntity
import com.umc.edison.remote.model.RemoteMapper

data class GetEditorPickResponse(
    @SerializedName("artletterId") val artLetterId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("readTime") val readTime: Int,
    @SerializedName("writer") val writer: String,
    @SerializedName("tags") val tags: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("likesCnt") val likesCnt: Int,
    @SerializedName("scrapsCnt") val scrapsCnt: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("liked") val liked : Boolean,
    @SerializedName("scraped") val scraped: Boolean
) : RemoteMapper<ArtLetterPreviewEntity> {
    override fun toData(): ArtLetterPreviewEntity = ArtLetterPreviewEntity(
        artLetterId = artLetterId,
        category = category,
        title = title,
        thumbnail = thumbnail,
        scraped = scraped,
        tags = tags.split(" ").filter { it.isNotBlank() }
    )
}

fun List<GetEditorPickResponse>.toData(): List<ArtLetterPreviewEntity> = map { it.toData() }
