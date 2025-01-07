package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BubbleLocal(
    val title: String?,
    val content: String?,
    val date: String?,
    var isDeleted: Boolean = false,
    var isSynced: Boolean = false,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
