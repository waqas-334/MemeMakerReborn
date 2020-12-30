package com.androidbull.meme.generator.data.db.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.androidbull.meme.generator.data.db.room.entity.MemeWithCaptionSets
import com.androidbull.meme.generator.data.db.room.entity.MemeWithSearchTags
import com.androidbull.meme.generator.model.Caption
import com.androidbull.meme.generator.model.CaptionSet2
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.model.SearchTag2


@Dao
abstract class MemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertMeme(meme: Meme2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertMemes(memes: List<Meme2>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertSearchTags(searchTag: List<SearchTag2>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertCaptions(captions: List<Caption>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertCaptionSets(captionSets: List<CaptionSet2>)


    fun insert(meme: Meme2) {

        val tempSearchTagsList = mutableListOf<SearchTag2>()
        val tempCaptionList = mutableListOf<Caption>()
        val tempCaptionSetList = mutableListOf<CaptionSet2>()

        meme.captionSets.forEach { captionSet ->
            if (captionSet.captions.isNotEmpty()) {
                tempCaptionSetList.add(captionSet)
                captionSet.captions.forEach { caption ->
                    tempCaptionList.add(caption)
                }
            }
        }
        meme.searchTags.forEach { searchTag ->
            tempSearchTagsList.add(searchTag)
        }

        insertMeme(tempCaptionSetList, tempCaptionList, tempSearchTagsList, meme)

    }

    @Transaction
    @Insert
    private fun insertMeme(
        captionSets: List<CaptionSet2>,
        captions: List<Caption>,
        searchTag2: List<SearchTag2>,
        meme: Meme2
    ) {
        insertMeme(meme)
        if (searchTag2.isNotEmpty())
            insertSearchTags(searchTag2)
        if (captions.isNotEmpty())
            insertCaptions(captions)
        if (captionSets.isNotEmpty())
            insertCaptionSets(captionSets)
    }

    fun insertAll(memes: List<Meme2>) {

        val tempSearchTagsList = mutableListOf<SearchTag2>()
        val tempCaptionList = mutableListOf<Caption>()
        val tempCaptionSetList = mutableListOf<CaptionSet2>()

        memes.forEach { meme2 ->

            meme2.captionSets.forEach { captionSet ->
                if (captionSet.captions.isNotEmpty()) {
                    tempCaptionSetList.add(captionSet)
                    captionSet.captions.forEachIndexed { index2, caption ->
                        tempCaptionList.add(caption)
                    }
                }
            }

            meme2.searchTags.forEach { searchTag ->
                tempSearchTagsList.add(searchTag)
            }
        }

        insertAllData(tempCaptionSetList, tempCaptionList, tempSearchTagsList, memes)

    }

    @Transaction
    @Insert
    private fun insertAllData(
        captionSets: List<CaptionSet2>,
        captions: List<Caption>,
        searchTag2: List<SearchTag2>,
        memes: List<Meme2>
    ) {
        if (memes.isNotEmpty())
            insertMemes(memes)
        if (searchTag2.isNotEmpty())
            insertSearchTags(searchTag2)
        if (captions.isNotEmpty())
            insertCaptions(captions)
        if (captionSets.isNotEmpty())
            insertCaptionSets(captionSets)
    }

    @Update
    abstract fun update(meme: Meme2): Int

    @Update
    abstract fun update(meme: List<Meme2>)

    @Transaction
    @Delete
    fun delete(memeId: Long) {

        val meme = getMemeById(memeId)

        deleteByMemeId(memeId)
        deleteSearchTagsByMemeId(memeId)

        meme.captionSets.forEach {
            deleteCaptionsByCaptionSetId(it.id)
        }
        deleteCaptionSetsByMemeId(memeId)
    }

    @Transaction
    @Delete
    fun delete(memes: List<Meme2>) {

        memes.forEach {
            deleteByMemeId(it.id)
            deleteSearchTagsByMemeId(it.id)
            it.captionSets.forEach { captionSet ->
                deleteCaptionsByCaptionSetId(captionSet.id)
            }
            deleteCaptionSetsByMemeId(it.id)
        }
    }

    @Query("DELETE FROM meme2 WHERE id = :memeId")
    protected abstract fun deleteByMemeId(memeId: Long)

    @Query("DELETE FROM searchtag2 WHERE memeId = :memeId")
    protected abstract fun deleteSearchTagsByMemeId(memeId: Long)

    @Query("DELETE FROM captionset2 WHERE memeId = :memeId")
    protected abstract fun deleteCaptionSetsByMemeId(memeId: Long)


    //TODO not working
    @Query("DELETE FROM caption WHERE captionSetId = :captionSetId")
    protected abstract fun deleteCaptionsByCaptionSetId(captionSetId: Long)


    @Query("SELECT * FROM meme2 WHERE id = :memeId")
    abstract fun getMemeById(memeId: Long): Meme2

    @Query("SELECT id FROM meme2 WHERE isCreatedByUser = 0 ORDER BY id DESC LIMIT 1;")
    abstract fun getLastMemeId(): Long

    @Query("SELECT * FROM meme2 WHERE id = :memeId")
    protected abstract fun getMemeWithSearchTagsInternal(memeId: Long): MemeWithSearchTags
    fun getMemeWithSearchTags(memeId: Long): Meme2 {
        val memeWithSearchTags = getMemeWithSearchTagsInternal(memeId)
        val meme = Meme2()

        meme.id = memeWithSearchTags.meme.id
        meme.imageName = memeWithSearchTags.meme.imageName
        meme.imageTitle = memeWithSearchTags.meme.imageTitle
        meme.isFavourite = memeWithSearchTags.meme.isFavourite
        meme.isModernMeme = memeWithSearchTags.meme.isModernMeme
        meme.isCreatedByUser = memeWithSearchTags.meme.isCreatedByUser
        meme.searchTags = memeWithSearchTags.searchTags
        return meme
    }

    @Query("SELECT * FROM meme2 WHERE id = :memeId")
    protected abstract fun getMemeWithCaptionSetsInternal(memeId: Long): MemeWithCaptionSets
    fun getMemeWithCaptionSets(memeId: Long): Meme2 {
        val memeWithCaptionSets = getMemeWithCaptionSetsInternal(memeId)
        val meme = Meme2()

        meme.id = memeWithCaptionSets.meme.id
        meme.imageName = memeWithCaptionSets.meme.imageName
        meme.imageTitle = memeWithCaptionSets.meme.imageTitle
        meme.isFavourite = memeWithCaptionSets.meme.isFavourite
        meme.isModernMeme = memeWithCaptionSets.meme.isModernMeme
        meme.isCreatedByUser = memeWithCaptionSets.meme.isCreatedByUser

        memeWithCaptionSets.captionSetsWithCaptions.forEach { captionSetWithCaptions ->
            val captionSet = CaptionSet2()
            captionSet.id = captionSetWithCaptions.captionSet.id
            captionSet.memeId = captionSetWithCaptions.captionSet.memeId
            captionSet.captions = captionSetWithCaptions.captions
            meme.captionSets.add(captionSet)
        }

        return meme
    }

    @Query("SELECT * FROM meme2")
    abstract fun getAllMemes(): MutableList<Meme2>

    @Query("SELECT * FROM meme2")
    protected abstract fun getAllMemesWithSearchTagsInternal(): MutableList<MemeWithSearchTags>
    fun getAllMemesWithSearchTags(): MutableList<Meme2> {
        val memesWithSearchTags = getAllMemesWithSearchTagsInternal()
        val memes: MutableList<Meme2> = mutableListOf()

        memesWithSearchTags.forEach { memeWithSearchTags ->
            val meme = Meme2()
            meme.id = memeWithSearchTags.meme.id
            meme.imageName = memeWithSearchTags.meme.imageName
            meme.imageTitle = memeWithSearchTags.meme.imageTitle
            meme.isFavourite = memeWithSearchTags.meme.isFavourite
            meme.isModernMeme = memeWithSearchTags.meme.isModernMeme
            meme.isCreatedByUser = memeWithSearchTags.meme.isCreatedByUser
            meme.searchTags = memeWithSearchTags.searchTags
            memes.add(meme)
        }
        return memes
    }

    @Query("SELECT * FROM meme2")
    protected abstract fun getAllMemesWithCaptionSetsInternal(): MutableList<MemeWithCaptionSets>
    fun getAllMemesWithCaptionSets(): MutableList<Meme2> {
        val memesWithCaptionSets = getAllMemesWithCaptionSetsInternal()
        val memes: MutableList<Meme2> = mutableListOf()

        memesWithCaptionSets.forEach { memeWithCaptionSets ->
            val meme = Meme2()
            meme.id = memeWithCaptionSets.meme.id
            meme.imageName = memeWithCaptionSets.meme.imageName
            meme.imageTitle = memeWithCaptionSets.meme.imageTitle
            meme.isFavourite = memeWithCaptionSets.meme.isFavourite
            meme.isModernMeme = memeWithCaptionSets.meme.isModernMeme
            meme.isCreatedByUser = memeWithCaptionSets.meme.isCreatedByUser

            memeWithCaptionSets.captionSetsWithCaptions.forEach { captionSetWithCaptions ->
                val captionSet = CaptionSet2()
                captionSet.id = captionSetWithCaptions.captionSet.id
                captionSet.memeId = captionSetWithCaptions.captionSet.memeId
                captionSet.captions = captionSetWithCaptions.captions
                meme.captionSets.add(captionSet)
            }
            memes.add(meme)
        }
        return memes
    }


    @Query("SELECT * FROM meme2 ")
    abstract fun getAllMemesObservable(): LiveData<MutableList<Meme2>>

/*
    @Query("SELECT * FROM meme2")
    abstract fun getMemesWithCaptionSets(): List<MemeWithCaptionSets>

    fun getAllMemes(): LiveData<MutableList<Meme2>> {
        val memesWithCaptionSets = getMemesWithCaptionSets()
        val memes: LiveData<MutableList<Meme2>> = MutableLiveData(mutableListOf())

        memesWithCaptionSets.forEach { memeWithCaptionSet ->

            val meme = Meme2()
            meme.id = memeWithCaptionSet.meme.id
            meme.imageName = memeWithCaptionSet.meme.imageName
            meme.imageTitle = memeWithCaptionSet.meme.imageTitle
            meme.isFavourite = memeWithCaptionSet.meme.isFavourite
            meme.isModernMeme = memeWithCaptionSet.meme.isModernMeme
            meme.isCreatedByUser = memeWithCaptionSets.meme.isCreatedByUser


        *//*    memeWithCaptionSet.captionSetsWithCaptions.forEach { captionSetWithCaptions ->
                val captionSet = CaptionSet2()
                captionSet.id = captionSetWithCaptions.captionSet.id
                captionSet.memeId = captionSetWithCaptions.captionSet.memeId
                captionSet.captions = captionSetWithCaptions.captions
                meme.captionSets.add(captionSet)
            }
*//*
            memes.value?.add(meme)
        }
        return memes
    }*/
/*
    fun getFavouriteMemes(): List<Meme2> {
        val memesWithCaptionSets = getMemesWithCaptionSets()
        val memes = mutableListOf<Meme2>()

        memesWithCaptionSets.forEach { memeWithCaptionSet ->

            if (memeWithCaptionSet.meme.isFavourite) {
                val meme = Meme2()
                meme.id = memeWithCaptionSet.meme.id
                meme.imageName = memeWithCaptionSet.meme.imageName
                meme.imageTitle = memeWithCaptionSet.meme.imageTitle
                meme.isFavourite = memeWithCaptionSet.meme.isFavourite
                meme.isModernMeme = memeWithCaptionSet.meme.isModernMeme
                meme.isCreatedByUser = memeWithCaptionSets.meme.isCreatedByUser


                memeWithCaptionSet.captionSetsWithCaptions.forEach { captionSetWithCaptions ->
                    val captionSet = CaptionSet2()
                    captionSet.id = captionSetWithCaptions.captionSet.id
                    captionSet.memeId = captionSetWithCaptions.captionSet.memeId
                    captionSet.captions = captionSetWithCaptions.captions
                    meme.captionSets.add(captionSet)
                }
                memes.add(meme)
            }
        }
        return memes
    }*/


}