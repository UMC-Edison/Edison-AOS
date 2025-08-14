package com.umc.edison.presentation.model

data class ContentBlockModel(
    val type: ContentType,
    var content: String,
    var position: Int,
    var imageKey: String? = null
) {
    fun toDomain(): String {
        // Text 타입의 경우 앞에 %<TEXT>와 뒤에 </TEXT>%가 붙어있고
        // Image 타입의 경우 앞에 %<IMAGE>와 뒤에 </IMAGE>%가 붙어있음
        return when (type) {
            ContentType.TEXT -> "%<TEXT>$content</TEXT>%"
            ContentType.IMAGE -> {
                val payload = if (!imageKey.isNullOrBlank()) "$content|$imageKey" else content
                "%<IMAGE>$payload</IMAGE>%"
            }
        }
    }
}

enum class ContentType {
    TEXT, IMAGE
}
