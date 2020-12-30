package com.androidbull.meme.generator.data.db.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.androidbull.meme.generator.model.Caption
import com.androidbull.meme.generator.model.CaptionSet2

class CaptionSetWithCaptions(

 @Embedded val captionSet: CaptionSet2,
 @Relation(
        parentColumn = "id",
        entityColumn = "captionSetId"
    )
    val captions: MutableList<Caption>
)