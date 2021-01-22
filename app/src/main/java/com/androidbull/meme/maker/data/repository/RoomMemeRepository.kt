package com.androidbull.meme.maker.data.repository

import android.Manifest
import android.content.ContentUris
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import com.androidbull.meme.maker.data.db.room.AppDatabase
import com.androidbull.meme.maker.helper.AppContext
import com.androidbull.meme.maker.helper.StorageHelper
import com.androidbull.meme.maker.model.CaptionSet2
import com.androidbull.meme.maker.model.Meme2
import java.io.File
import java.io.IOException

class RoomMemeRepository : IMemeRepository {

    private val database = AppDatabase.getInstance(AppContext.getInstance().context)

    override fun insertMeme(meme: Meme2) {
        database.memeDao().insert(meme)
    }

    override fun insertAllMemes(memes: List<Meme2>) {
        database.memeDao().insertAll(memes)
    }


    override fun updateMeme(meme: Meme2) = database.memeDao().update(meme)

    override fun updateMemeCaptions(memeId: Long, captionSet2: List<CaptionSet2>) =
        database.memeDao().updateMemeCaptions(
            memeId,
            captionSet2
        )


    override fun updateMemes(memes: List<Meme2>) {
        database.memeDao().update(memes)
    }

    override fun deleteMeme(memeId: Long) {

    }

    override fun deleteMemes(memeIds: List<Long>) {
        database.memeDao().delete(memeIds)

    }

    override fun getMemeById(memeId: Long) = database.memeDao().getMemeById(memeId)
    override fun getLastMemeId() = database.memeDao().getLastMemeId()

    override fun getMemeWithSearchTags(memeId: Long) =
        database.memeDao().getMemeWithSearchTags(memeId)

    override fun getMemeWithCaptionSets(memeId: Long) =
        database.memeDao().getMemeWithCaptionSets(memeId)


    override fun getAllMemes() = database.memeDao().getAllMemes()
    override fun getAllMemesWithSearchTags() = database.memeDao().getAllMemesWithSearchTags()
    override fun getAllMemeWithCaptionSets() = database.memeDao().getAllMemesWithCaptionSets()


    override fun getAllMemesObservable() = database.memeDao().getAllMemesObservable()


    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE])
    override fun getSavedMemes(): MutableList<Meme2> {
        val tempSavedMemes = mutableListOf<Meme2>()

        try {

            // using DATA column for both Q and below Q
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

            AppContext.getInstance().context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->

                // Cache column indices.
                val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val pathColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val nameColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val path = cursor.getString(pathColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getString(sizeColumn)

                    val uri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    // Discard invalid images that might exist on the device
                    if (size == null) {
                        continue
                    }

                    if (path.contains("MemeMaker")) {

                        val tempFile = File(path)
                        if (tempFile.exists()) {
                            val meme2 = Meme2()
                            meme2.id =
                                System.currentTimeMillis()   // assigns same id to multiple instances, so Thread.sleep
                            meme2.imageName = uri.toString()
                            meme2.imageTitle = name
                            tempSavedMemes.add(meme2)
                            Thread.sleep(1)     // assigns same id to multiple instances, so Thread.sleep
                        }
                    }
                }
            }


        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return tempSavedMemes

        // Android Q and above, it will only display app's created images
        /* val projection = arrayOf(
             MediaStore.Images.Media._ID,
             MediaStore.Images.Media.DISPLAY_NAME,
             MediaStore.Images.Media.RELATIVE_PATH,
             MediaStore.Images.Media.SIZE,
         )

         val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

         AppContext.getInstance().context.contentResolver.query(
             MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
             projection,
             null,
             null,
             sortOrder
         )?.use { cursor ->

              // Cache column indices.
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val pathColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val nameColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

             while (cursor.moveToNext()) {
                 val id = cursor.getLong(idColumn)
                 val path = cursor.getString(pathColumn)
                 val name = cursor.getString(nameColumn)
                val size = cursor.getString(sizeColumn)

                 val uri =
                     ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                 // Discard invalid images that might exist on the device
                 if (size == null) {
                     continue
                 }

                 val meme2 = Meme2()
                 meme2.id =
                     System.currentTimeMillis()   // assign same id to multiple instances, so Thread.sleep
                 meme2.imageName = uri.toString()
                 meme2.imageTitle = name
                 tempSavedMemes.add(meme2)
                 Thread.sleep(1)
             }

             cursor.close()
         }*/

    }

    override fun deleteSavedMemes(memes: List<Meme2>): Boolean {

    return false
    }

    override fun deleteSavedTemplates(memes: List<Meme2>) {
        val memesToDelete =
            mutableListOf<Long>()      // memes which are successfully deleted from storage
        if (StorageHelper.isExternalStorageWriteable()) {       //first delete from storage
            try {
                memes.forEach { meme ->
                    val memeTemplateFile =
                        File(StorageHelper.getTemplatesPrivateDir() + meme.imageName)
                    if (memeTemplateFile.exists()) {
                        if (memeTemplateFile.delete()) {
                            memesToDelete.add(meme.id)
                        }
                    } else {
                        memesToDelete.add(meme.id)
                    }
                }
                deleteMemes(memesToDelete)
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: SecurityException) {
                ex.printStackTrace()
            }
        }
    }
}