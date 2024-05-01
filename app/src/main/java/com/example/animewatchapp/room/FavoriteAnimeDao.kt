package com.example.animewatchapp.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animewatchapp.model.Anime
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAnimeDao {
    @Query("SELECT * FROM favorite_anime")
    fun getAllFavoriteAnime(): Flow<List<Anime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteAnime(anime: Anime)

    @Delete
    suspend fun deleteFavoriteAnime(anime: Anime)

    @Query("SELECT EXISTS (SELECT * FROM favorite_anime where animeLink = :animeLink)")
    suspend fun checkAnimeFavorite(animeLink: String): Boolean

    @Query("SELECT * FROM favorite_anime where animeLink = :animeLink")
    suspend fun getFavoriteAnime(animeLink: String): Anime
}