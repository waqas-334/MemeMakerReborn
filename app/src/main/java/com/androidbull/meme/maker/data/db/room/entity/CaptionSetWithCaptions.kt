package com.androidbull.meme.maker.data.db.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.androidbull.meme.maker.model.Caption
import com.androidbull.meme.maker.model.CaptionSet2

class CaptionSetWithCaptions(

 @Embedded val captionSet: CaptionSet2,
 @Relation(
        parentColumn = "id",
        entityColumn = "captionSetId"
    )
    val captions: MutableList<Caption>
)