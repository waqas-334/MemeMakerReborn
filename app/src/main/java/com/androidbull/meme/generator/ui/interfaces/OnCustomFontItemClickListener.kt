package com.androidbull.meme.generator.ui.interfaces

import com.androidbull.meme.generator.model.CaptionFont

interface OnCustomFontItemClickListener {
    fun onCustomFontDeleteClicked(font: CaptionFont, position: Int)
}