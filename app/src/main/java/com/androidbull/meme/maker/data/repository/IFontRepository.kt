package com.androidbull.meme.maker.data.repository

import com.androidbull.meme.maker.model.CaptionFont


interface IFontRepository {

    private val classicFontId: Long
        get() = 1L
    private val defaultFontId: Long
        get() = 2L

    fun getAll(): MutableList<CaptionFont>
    fun getCustomFonts(): MutableList<CaptionFont>
    fun getById(fontId: Long): CaptionFont
    fun insertAll(captionFonts: List<CaptionFont>)
    fun insert(captionFont: CaptionFont): Long
    fun delete(captionFont: CaptionFont): Int
    fun update(captionFont: CaptionFont)
    fun getClassicMemeFont() = getById(classicFontId)
    fun getDefaultFont() = getById(defaultFontId)


}