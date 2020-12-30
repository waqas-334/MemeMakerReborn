package com.androidbull.meme.generator.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class CaptionSet2() : Serializable {
    @PrimaryKey
    var id: Long = 0
    var memeId: Long = 0

    @Ignore
    var captions: MutableList<Caption> = mutableListOf()

    constructor(id: Long, memeId: Long, captions: MutableList<Caption>) : this() {
        this.id = id
        this.memeId = memeId
        this.captions = captions
    }
}