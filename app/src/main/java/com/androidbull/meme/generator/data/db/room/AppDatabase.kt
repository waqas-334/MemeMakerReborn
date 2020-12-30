package com.androidbull.meme.generator.data.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.androidbull.meme.generator.data.db.room.dao.*
import com.androidbull.meme.generator.model.*

private const val DATABASE_NAME = "MemeMaker.db"

@Database(
    entities = [Meme2::class, SearchTag2::class, CaptionSet2::class, Caption::class, CaptionFont::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memeDao(): MemeDao
    abstract fun captionDao(): CaptionDao
    abstract fun searchTagDao(): SearchTagDao
    abstract fun captionSetDao(): CaptionSetDao
    abstract fun captionFontDao(): CaptionFontDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .createFromAsset(DATABASE_NAME)
                 /*.addCallback(
                     object : RoomDatabase.Callback() {
                         override fun onCreate(db: SupportSQLiteDatabase) {
                             super.onCreate(db)
                             val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                             WorkManager.getInstance(context).enqueue(request)
                         }
                     }
                 )*/
                .build()
        }
    }
}