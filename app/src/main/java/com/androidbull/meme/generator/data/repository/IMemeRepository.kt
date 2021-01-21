package com.androidbull.meme.generator.data.repository

import androidx.lifecycle.LiveData
import com.androidbull.meme.generator.model.Meme2


interface IMemeRepository {

    fun insertMeme(meme: Meme2)
    fun insertAllMemes(memes: List<Meme2>)

    fun updateMeme(meme: Meme2): Int
    fun updateMemes(memes: List<Meme2>)

    fun deleteMeme(memeId: Long)
    fun deleteMemes(memes: List<Meme2>)

    fun getMemeById(memeId: Long): Meme2?
    fun getLastMemeId(): Long
    fun getMemeWithSearchTags(memeId: Long): Meme2?
    fun getMemeWithCaptionSets(memeId: Long): Meme2?

    fun getAllMemes(): MutableList<Meme2>
    fun getAllMemesWithSearchTags(): MutableList<Meme2>
    fun getAllMemeWithCaptionSets(): MutableList<Meme2>

    fun getAllMemesObservable(): LiveData<MutableList<Meme2>>

    fun getSavedMemes(): MutableList<Meme2>
    fun deleteSavedMemes(memes: List<Meme2>): Boolean
    fun deleteSavedTemplates(memes: List<Meme2>)


}