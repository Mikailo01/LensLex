package com.bytecause.lenslex.di

import android.content.Context
import androidx.room.Room
import com.bytecause.lenslex.data.local.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "app_database"
    )
        .createFromAsset("database/continents_with_countries.db")
        .build()

    /*private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

        }
    }*/

    // Provide DAOs.
    @Provides
    @Singleton
    fun provideWordDao(db: AppDatabase) = db.wordDao()
}