package com.androidbull.meme.maker.ui.interfaces

import com.androidbull.meme.maker.model.Meme2

interface OnSavedMemeItemClickListener {
    fun onMemeClicked(meme: Meme2, position: Int)
    fun onMemeSelected(meme: Meme2, position: Int, isSelected: Boolean)
}