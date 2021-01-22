package com.androidbull.meme.maker.data.db.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.androidbull.meme.maker.model.CaptionSet2
import com.androidbull.meme.maker.model.Meme2

class MemeWithCaptionSets(

    @Embedded val meme: Meme2,
    @Relation(
        parentColumn = "id",
        entityColumn = "memeId",
        entity = CaptionSet2::class
    )
    val captionSetsWithCaptions: MutableList<CaptionSetWithCaptions>
)