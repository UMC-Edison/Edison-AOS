package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageLocal(
    val uri: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
