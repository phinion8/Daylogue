package com.phinion.dailyjournal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phinion.dailyjournal.data.ImageToDeleteDao
import com.phinion.dailyjournal.data.database.entity.ImageToDelete
import com.phinion.dailyjournal.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao
}