package com.androidbull.meme.maker.data.db.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.androidbull.meme.maker.model.Meme2
import com.androidbull.meme.maker.model.SearchTag2

class MemeWithSearchTags(

    @Embedded val meme: Meme2,
    @Relation(
        parentColumn = "id",
        entityColumn = "memeId",
        entity = SearchTag2::class
    )
    val searchTags: MutableList<SearchTag2>
)