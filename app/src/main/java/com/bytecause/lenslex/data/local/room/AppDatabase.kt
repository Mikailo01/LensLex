package com.bytecause.lenslex.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity

@Database(
    entities = [
        WordAndSentenceEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
}