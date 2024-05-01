package com.example.animewatchapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDetails
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class AnimeDetailsUiState(
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val animeDetails: AnimeDetails? = null
)

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(AnimeDetailsUiState())
    val animeUiState: StateFlow<AnimeDetailsUiState> = _animeUiState

    fun getAnimeDetails(contentLink: String) {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        animeRepository.getAnimeDetails(contentLink).apply {
                            if(this == null) {
                                _animeUiState.update {
                                    it.copy(isError = true, isLoading = false)
                                }
                            }
                            else {
                                _animeUiState.update {
                                    it.copy(animeDetails = this, isLoading = false, isError = false)
                                }
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _animeUiState.update {
                    it.copy(isError = true, isLoading = false)
                }
            }
        }
    }

    suspend fun getAnimeStreamLink(animeLink: String, animeEpisode: Int): String =
        withContext(Dispatchers.IO) {
            animeRepository.getStreamLink(animeLink, animeEpisode.toString()).apply {
                return@withContext this
            }
        }

    fun addDownloadAnime(animeDownload: AnimeDownload) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.addDownloadAnime(animeDownload)
            }
        }
    }

    fun checkAnimeFavorite(animeLink: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.checkAnimeFavorite(animeLink).apply {
                    _animeUiState.update {
                        it.copy(isFavorite = this)
                    }
                }
            }
        }
    }

    fun addFavoriteAnime(anime: Anime) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.addFavoriteAnime(anime)
                _animeUiState.update {
                    it.copy(isFavorite = true)
                }
            }
        }
    }

    fun removeFavoriteAnime(animeLink: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.deleteFavoriteAnime(animeLink)
                _animeUiState.update {
                    it.copy(isFavorite = false)
                }
            }
        }
    }
}