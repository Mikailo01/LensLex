package com.bytecause.lenslex.di

import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSupportedLanguagesRepository(): SupportedLanguagesRepository = SupportedLanguagesRepository()
}