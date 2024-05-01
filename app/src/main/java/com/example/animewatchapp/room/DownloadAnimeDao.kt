package com.example.animewatchapp.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animewatchapp.model.AnimeDownload
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadAnimeDao {
    @Query("SELECT * FROM download_anime")
    fun getAllDownloadAnime(): Flow<List<AnimeDownload>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDownloadAnime(anime: AnimeDownload)

    @Query("SELECT EXISTS (SELECT * FROM download_anime where animeLink = :animeLink AND animeEpisode = :episode)")
    fun checkDownloadAnime(animeLink: String, episode: Int): Boolean

    @Query("SELECT * FROM download_anime WHERE animeName LIKE '%' || :animeName || '%'")
    fun searchDownloadAnime(animeName: String): Flow<List<AnimeDownload>>

    @Query("DELETE FROM download_anime WHERE animeLink = :animeLink")
    fun deleteDownloadAnime(animeLink: String)

    @Query("DELETE FROM download_anime")
    fun deleteAllDownloadAnime()
}