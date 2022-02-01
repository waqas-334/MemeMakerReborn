package com.androidbull.meme.maker.data.db.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.androidbull.meme.maker.data.db.room.dao.*
import com.androidbull.meme.maker.model.*

private const val DATABASE_NAME = "MemeMaker.db"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE meme2 ADD COLUMN imageUrl TEXT NOT NULL DEFAULT \"\""
//            "CREATE TABLE `meme2` " +x
//                    "(`id` LONG, " +
//                    "`imageName` TEXT, " +
//                    "`imageTitle` TEXT, " +
//                    "`isFavourite` BOOLEAN, " +
//                    "`isModernMeme` BOOLEAN, " +
//                    "`isCreatedByUser` BOOLEAN, " +
//                    "`imageUrl` TEXT, " +
//                    "PRIMARY KEY(`id`))"
        )
    }
}
@Database(
    entities = [Meme2::class, SearchTag2::class, CaptionSet2::class, Caption::class, CaptionFont::class],
    version = 2,
    exportSchema = true
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
                .addMigrations(MIGRATION_1_2)
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

