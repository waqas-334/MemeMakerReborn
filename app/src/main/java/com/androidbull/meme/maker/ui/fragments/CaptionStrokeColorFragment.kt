package com.androidbull.meme.maker.ui.fragments

import android.graphics.Color
import com.androidbull.meme.maker.R

class CaptionStrokeColorFragment : BaseCaptionColorFragment() {

    override fun getLayoutId() = R.layout.fragment_caption_color
    override fun getInitialColor() = Color.parseColor(captionSetting.strokeColor)

}