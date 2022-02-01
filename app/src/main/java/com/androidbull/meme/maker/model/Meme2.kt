package com.androidbull.meme.maker.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidbull.meme.maker.helper.MEME_SERVER_BASE_URL
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
    var imageUrl: String = ""

    @Ignore
    var searchTags: MutableList<SearchTag2> = mutableListOf()

    @Ignore
    var captionSets: MutableList<CaptionSet2> = mutableListOf()

    override fun toString() =
        "ID: $id\nimageName: $imageName\nimageTitle: $imageTitle\nisFavourite: $isFavourite\nisModernMeme: $isModernMeme\nisCreatedByUser: $isCreatedByUser\nimageUrl: $imageUrl"

    @Ignore
    fun getMemeUrl() = if(imageUrl.isEmpty()) MEME_SERVER_BASE_URL+imageName else imageUrl

}