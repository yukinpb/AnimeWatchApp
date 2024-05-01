package com.example.animewatchapp.repository

import android.util.Log
import com.example.animewatchapp.R
import com.example.animewatchapp.datasource.AnimeSource
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDetails
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.model.AnimeHistory
import com.example.animewatchapp.room.DownloadAnimeDao
import com.example.animewatchapp.room.FavoriteAnimeDao
import com.example.animewatchapp.room.HistoryAnimeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

interface AnimeRepository {
    suspend fun getAnimeDetails(contentLink: String): AnimeDetails?
    suspend fun searchAnime(searchUrl: String): ArrayList<Anime>
    suspend fun getLatestAnime(page: Int): ArrayList<Anime>
    suspend fun getTrendingAnime(): ArrayList<Anime>
    suspend fun getStreamLink(animeUrl: String, animeEpisode: String): String

    suspend fun getAllFavoriteAnime(): Flow<List<Anime>>
    suspend fun checkAnimeFavorite(animeLink: String): Boolean
    suspend fun addFavoriteAnime(anime: Anime)
    suspend fun deleteFavoriteAnime(animeLink: String)

    suspend fun getAllHistoryAnime(): Flow<List<AnimeHistory>>
    suspend fun checkHistoryAnime(animeLink: String): Boolean
    suspend fun addHistoryAnime(anime: AnimeHistory)
    suspend fun updateHistoryAnime(animeLink: String, animeEpisode: Int)

    suspend fun getAllDownloadAnime(): Flow<List<AnimeDownload>>
    suspend fun checkDownloadAnime(animeLink: String, episode: Int): Boolean
    suspend fun addDownloadAnime(anime: AnimeDownload)
    suspend fun searchDownloadAnime(animeName: String): Flow<List<AnimeDownload>>
    suspend fun deleteDownloadAnime(animeLink: String)
    suspend fun deleteAllDownloadAnime()
}

class AnimeRepositoryImpl @Inject constructor(
    private val favoriteAnimeDao: FavoriteAnimeDao,
    private val historyAnimeDao: HistoryAnimeDao,
    private val downloadAnimeDao: DownloadAnimeDao
) : AnimeRepository {
    private val animeSource = AnimeSource()

    override suspend fun getAnimeDetails(contentLink: String): AnimeDetails? =
        withContext(Dispatchers.IO) {
            try {
                animeSource.animeDetails(contentLink)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        }

    override suspend fun searchAnime(searchUrl: String): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
             try {
                animeSource.searchAnime(searchUrl)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                ArrayList()
            }
        }

    override suspend fun getLatestAnime(page: Int): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
            try {
                animeSource.latestAnime(page)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                ArrayList()
            }
        }

    override suspend fun getTrendingAnime(): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
            try {
                animeSource.trendingAnime()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                ArrayList()
            }
        }

    override suspend fun getStreamLink(animeUrl: String, animeEpisode: String): String =
        withContext(Dispatchers.IO) {
            try {
                animeSource.animeLink(animeUrl, animeEpisode)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                ""
            }
        }

    override suspend fun getAllFavoriteAnime(): Flow<List<Anime>> =
        withContext(Dispatchers.IO) {
            favoriteAnimeDao.getAllFavoriteAnime()
        }

    override suspend fun checkAnimeFavorite(animeLink: String): Boolean =
        withContext(Dispatchers.IO) {
            favoriteAnimeDao.checkAnimeFavorite(animeLink)
        }

    override suspend fun addFavoriteAnime(anime: Anime) =
        withContext(Dispatchers.IO) {
            favoriteAnimeDao.addFavoriteAnime(anime)
        }

    override suspend fun deleteFavoriteAnime(animeLink: String) = withContext(Dispatchers.IO) {
        val anime = favoriteAnimeDao.getFavoriteAnime(animeLink)
        favoriteAnimeDao.deleteFavoriteAnime(anime)
    }

    override suspend fun getAllHistoryAnime(): Flow<List<AnimeHistory>> =
        withContext(Dispatchers.IO) {
            historyAnimeDao.getAllHistoryAnime()
        }

    override suspend fun checkHistoryAnime(animeLink: String): Boolean =
        withContext(Dispatchers.IO) {
            historyAnimeDao.checkAnimeHistory(animeLink)
        }

    override suspend fun addHistoryAnime(anime: AnimeHistory) =
        withContext(Dispatchers.IO) {
            historyAnimeDao.addHistoryAnime(anime)
        }

    override suspend fun updateHistoryAnime(animeLink: String, animeEpisode: Int) =
        withContext(Dispatchers.IO) {
            val lastWatched = Date()
            historyAnimeDao.updateHistoryAnime(animeLink, animeEpisode, lastWatched)
        }

    override suspend fun getAllDownloadAnime(): Flow<List<AnimeDownload>> =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.getAllDownloadAnime()
        }


    override suspend fun checkDownloadAnime(animeLink: String, episode: Int): Boolean =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.checkDownloadAnime(animeLink, episode)
        }

    override suspend fun addDownloadAnime(anime: AnimeDownload) =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.addDownloadAnime(anime)
        }

    override suspend fun searchDownloadAnime(animeName: String): Flow<List<AnimeDownload>> =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.searchDownloadAnime(animeName)
        }

    override suspend fun deleteDownloadAnime(animeLink: String) =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.deleteDownloadAnime(animeLink)
        }

    override suspend fun deleteAllDownloadAnime() =
        withContext(Dispatchers.IO) {
            downloadAnimeDao.deleteAllDownloadAnime()
        }

    companion object {
        val TAG = R.string.app_name.toString()
    }
}