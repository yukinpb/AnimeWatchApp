package com.example.animewatchapp.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.model.AnimeHistory
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [Anime::class, AnimeHistory::class, AnimeDownload::class], version = 3)
@TypeConverters(Converters::class)
abstract class AnimeDatabase: RoomDatabase() {
    abstract fun favoriteAnimeDao(): FavoriteAnimeDao
    abstract fun historyAnimeDao(): HistoryAnimeDao
    abstract fun downloadAnimeDao(): DownloadAnimeDao
}