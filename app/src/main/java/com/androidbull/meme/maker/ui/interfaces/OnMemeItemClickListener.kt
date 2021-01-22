package com.androidbull.meme.maker.ui.interfaces

import com.androidbull.meme.maker.model.Meme2

interface OnMemeItemClickListener {
    fun onMemeClicked(meme: Meme2, position: Int)
    fun onAddToFavouritesClicked(meme: Meme2, position: Int, isFavourite: Boolean)
}