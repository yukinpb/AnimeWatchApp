package com.example.animewatchapp.di

import android.app.Application
import androidx.room.Room
import com.example.animewatchapp.room.AnimeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        application: Application
    ): AnimeDatabase {
        return Room.databaseBuilder(application, AnimeDatabase::class.java, "anime-db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteAnimeDao(animeDatabase: AnimeDatabase) = animeDatabase.favoriteAnimeDao()

    @Provides
    @Singleton
    fun provideHistoryAnimeDao(animeDatabase: AnimeDatabase) = animeDatabase.historyAnimeDao()

    @Provides
    @Singleton
    fun provideDownloadAnimeDao(animeDatabase: AnimeDatabase) = animeDatabase.downloadAnimeDao()
}