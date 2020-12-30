package com.androidbull.meme.generator.ui.interfaces

import com.androidbull.meme.generator.model.Meme2

interface OnSavedMemeItemClickListener {
    fun onMemeClicked(meme: Meme2, position: Int)
    fun onMemeSelected(meme: Meme2, position: Int, isSelected: Boolean)
}