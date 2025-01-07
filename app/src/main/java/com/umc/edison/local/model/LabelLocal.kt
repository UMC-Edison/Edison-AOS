package com.umc.edison.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LabelLocal(
    val name: String,
    val color: String,
    var isDeleted: Boolean,
    var isSynced: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
