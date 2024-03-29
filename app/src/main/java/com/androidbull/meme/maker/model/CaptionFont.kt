package com.androidbull.meme.maker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CaptionFont(
    @PrimaryKey
    val id: Long,
    val Name: String,
    val displayName: String,
    var isAppProvidedFont: Boolean = true
)