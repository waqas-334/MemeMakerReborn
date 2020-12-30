package com.androidbull.meme.generator.data.repository

import com.androidbull.meme.generator.data.db.room.AppDatabase
import com.androidbull.meme.generator.helper.AppContext
import com.androidbull.meme.generator.model.CaptionFont

class RoomFontRepository : IFontRepository {

    private val database = AppDatabase.getInstance(AppContext.getInstance().context)
    override fun getAll() = database.captionFontDao().getAll()
    override fun getCustomFonts() = database.captionFontDao().getCustomFonts()

    override fun getById(fontId: Long) = database.captionFontDao().getById(fontId)

    override fun insertAll(captionFonts: List<CaptionFont>) {
        database.captionFontDao().insertAll(captionFonts)
    }

    override fun insert(captionFont: CaptionFont) = database.captionFontDao().insert(captionFont)

    override fun delete(captionFont: CaptionFont) = database.captionFontDao().delete(captionFont)

    override fun update(captionFont: CaptionFont) {
        database.captionFontDao().update(captionFont)
    }

}