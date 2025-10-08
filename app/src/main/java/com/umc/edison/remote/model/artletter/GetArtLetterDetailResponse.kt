package com.umc.edison.remote.model.artletter

import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.artLetter.ArtLetterEntity
import com.umc.edison.remote.model.RemoteMapper

class GetArtLetterDetailResponse (
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
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("scraped") val scraped: Boolean
) : RemoteMapper<ArtLetterEntity> {

    override fun toData(): ArtLetterEntity = ArtLetterEntity(
        artLetterId = artLetterId,
        title = title,
        content = content,
        category = category,
        readTime = readTime,
        writer = writer,
        tags = tags.split(" ").filter { it.isNotBlank() },
        thumbnail = thumbnail,
        likesCnt = likesCnt,
        liked = liked,
        scraped = scraped
    )
}
