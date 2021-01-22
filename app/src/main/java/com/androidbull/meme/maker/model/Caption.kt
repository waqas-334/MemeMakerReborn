package com.androidbull.meme.maker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Caption : Serializable {
    @PrimaryKey
    var id: Long = 0
    var captionSetId: Long? = null
    var text: String? = null
    var fontSize: Int? = null
    var strokeWidth: Int? = null
    var positionX: Float? = null
    var positionY: Float? = null
    var fontType: Long? = null
    var alignment: Int? = null
    var textColor: String? = null
    var strokeColor: String? = null
    var textWidth: Int? = null
    var maxLines: Int? = null
}
