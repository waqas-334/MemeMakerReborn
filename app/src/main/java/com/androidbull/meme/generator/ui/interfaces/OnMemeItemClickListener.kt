package com.androidbull.meme.generator.ui.interfaces

import com.androidbull.meme.generator.model.Meme2

interface OnMemeItemClickListener {
    fun onMemeClicked(meme: Meme2, position: Int)
    fun onAddToFavouritesClicked(meme: Meme2, position: Int, isFavourite: Boolean)
}