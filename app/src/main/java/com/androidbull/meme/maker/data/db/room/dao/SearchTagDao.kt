package com.androidbull.meme.maker.data.db.room.dao

import androidx.room.*
import com.androidbull.meme.maker.model.SearchTag2


@Dao
interface SearchTagDao {
    @Query("SELECT * FROM searchtag2")
    fun getAll(): List<SearchTag2>


    @Query("SELECT * FROM searchtag2 WHERE id IN (:searchTagIds)")
    fun loadAllByIds(searchTagIds: IntArray): List<SearchTag2>


    @Insert
    fun insert(vararg searchTags: SearchTag2)

    @Insert
    fun insertAll(searchTags: List<SearchTag2>)

    @Delete
    fun delete(searchTag: SearchTag2)

    @Update
    fun update(searchTag: SearchTag2)
}