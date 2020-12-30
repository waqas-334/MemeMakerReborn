package com.androidbull.meme.generator.data.db.room.dao

import androidx.room.*
import com.androidbull.meme.generator.model.CaptionFont


@Dao
interface CaptionFontDao {
    @Query("SELECT * FROM captionfont")
    fun getAll(): MutableList<CaptionFont>

    @Query("SELECT * FROM captionfont WHERE isAppProvidedFont = 0")
    fun getCustomFonts(): MutableList<CaptionFont>

    @Query("SELECT * FROM captionfont WHERE id = :fontId")
    fun getById(fontId: Long): CaptionFont

    @Insert
    fun insert(captionFont: CaptionFont): Long

    @Insert
    fun insertAll(captionFonts: List<CaptionFont>)

    @Delete
    fun delete(captionFont: CaptionFont): Int

    @Update
    fun update(captionFont: CaptionFont)
}