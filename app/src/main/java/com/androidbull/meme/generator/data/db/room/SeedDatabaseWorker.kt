package com.androidbull.meme.generator.data.db.room

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
           /* getJsonDataFromAsset(applicationContext, MEME_DATA_ASSET_FILE)?.let {
                val memeData2New = Gson().fromJson(it, MemeData2New::class.java)
                val database =
                    AppDatabase.getInstance(applicationContext)
                database.memeDao().insertAll(memeData2New.memes)
                database.captionFontDao().insertAll(FontHelper.getAllIntegralFonts())
            }*/
            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SeedDatabaseWorker"
    }
}