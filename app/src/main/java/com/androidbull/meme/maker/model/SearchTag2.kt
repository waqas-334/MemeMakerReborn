package com.androidbull.meme.maker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class SearchTag2() : Serializable {
    @PrimaryKey
    var id: Long = 0
    var memeId: Long = 0
    var text: String = ""


    constructor(id: Long, memeId: Long, text: String) : this() {
        this.id = id
        this.memeId = memeId
        this.text = text
    }
}