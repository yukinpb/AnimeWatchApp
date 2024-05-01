package com.example.animewatchapp.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animewatchapp.model.AnimeHistory
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface HistoryAnimeDao {
    @Query("SELECT * FROM history_anime")
    fun getAllHistoryAnime(): Flow<List<AnimeHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistoryAnime(anime: AnimeHistory)

    @Query("SELECT EXISTS (SELECT * FROM history_anime where animeLink = :animeLink)")
    suspend fun checkAnimeHistory(animeLink: String): Boolean

    @Query("UPDATE history_anime SET lastWatched = :lastWatched, animeEpisode = :animeEpisode where animeLink = :animeLink")
    suspend fun updateHistoryAnime(animeLink: String, animeEpisode: Int, lastWatched: Date)

}