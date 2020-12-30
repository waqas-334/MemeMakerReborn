package com.androidbull.meme.generator.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity()
class Meme2 : Serializable {
    @PrimaryKey
    var id: Long = 0
    var imageName: String = ""
    var imageTitle: String = ""
    var isFavourite: Boolean = false
    var isModernMeme: Boolean = false
    var isCreatedByUser: Boolean = false

    @Ignore
    var searchTags: MutableList<SearchTag2> = mutableListOf()

    @Ignore
    var captionSets: MutableList<CaptionSet2> = mutableListOf()

}