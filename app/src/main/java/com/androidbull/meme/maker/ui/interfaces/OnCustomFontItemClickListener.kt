package com.androidbull.meme.maker.ui.interfaces

import com.androidbull.meme.maker.model.CaptionFont

interface OnCustomFontItemClickListener {
    fun onCustomFontDeleteClicked(font: CaptionFont, position: Int)
}