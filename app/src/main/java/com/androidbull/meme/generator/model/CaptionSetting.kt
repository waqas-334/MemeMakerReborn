package com.androidbull.meme.generator.model

import ja.burhanrashid52.photoeditor.CAPTION_DEFAULT_POSITION

data class CaptionSetting(
    var font: CaptionFont?,
    var fontSize: Int,
    var maxLines: Int,
    var textColor: String,
    var strokeColor: String,
    var strokeWidth: Int,
    var positionX: Float,
    var positionY: Float
)

fun getDefaultCaptionSettings() =
    CaptionSetting(
        CaptionFont(1, "anton.ttf", "Anton / Impact", true),
        40,
        2,
        "#FFFFFFFF",
        "#FF000000",
        1,
        CAPTION_DEFAULT_POSITION,
        CAPTION_DEFAULT_POSITION
    )

fun getClassicMemeCaptionSetting() =
    CaptionSetting(
        CaptionFont(1, "anton.ttf", "Anton / Impact", true),
        40,
        2,
        "#FFFFFFFF",
        "#FF000000",
        1,
        CAPTION_DEFAULT_POSITION,
        CAPTION_DEFAULT_POSITION
    )
