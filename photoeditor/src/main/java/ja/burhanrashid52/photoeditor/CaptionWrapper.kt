package ja.burhanrashid52.photoeditor

import android.graphics.Typeface
import java.io.Serializable

class CaptionWrapper : Serializable {
    var id: Long = 0
    var captionSetId: Long? = null
    var text: String? = null
    var fontSize: Int? = null
    var strokeWidth: Int? = null
    var positionX: Float = 0f
    var positionY: Float = 0f
    var alignment: Int? = null
    var textColor: String? = null
    var strokeColor: String? = null
    var textWidth: Int? = null
    var maxLines: Int? = null
    var fontType: Long? = null
    var fontTypeFace: Typeface? = null
}
