package com.example.animewatchapp.viewmodels

import android.app.Application
import android.media.session.PlaybackState
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeHistory
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
import java.io.File
import javax.inject.Inject

data class AnimeStreamUiState(
    val isLoading: Boolean = false,
    var animeStreamLink: String? = null,
    val isPlaying: Boolean = false,
    var isNextEp: Boolean = false,
    val isError: Boolean = false,
    val isInHistory: Boolean = false
)

@UnstableApi @HiltViewModel
class AnimeStreamViewModel @Inject constructor(
    val app: Application,
    private val animeRepository: AnimeRepository,
    var player: Player
) : ViewModel(
) {
    private val _animeUiState = MutableStateFlow(AnimeStreamUiState())
    val animeUiState: StateFlow<AnimeStreamUiState> = _animeUiState

    init {
        player.prepare()
        player.playWhenReady = true
        player.addListener(getCustomPlayerListener())
    }

    fun getAnimeStreamLink(animeLink: String, animeEpisode: Int) {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        try {
            viewModelScope.launch {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        animeRepository.getStreamLink(animeLink, animeEpisode.toString()).apply {
                            if(this.isEmpty()) {
                                _animeUiState.update {
                                    it.copy(isError = true, isLoading = false)
                                }
                            }
                            else {
                                _animeUiState.update {
                                    it.copy(animeStreamLink = this, isLoading = false, isNextEp = false, isError = false)
                                }
                                Log.d("AnimeStreamViewModel", "getAnimeStreamLink: $this")
                                withContext(Dispatchers.Main) {
                                    playAnime()
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (e: TimeoutCancellationException) {
            _animeUiState.update {
                it.copy(isError = true, isLoading = false)
            }
        }

    }

    private suspend fun playAnime() =
        withContext(Dispatchers.Main) {
            if (_animeUiState.value.animeStreamLink == null) return@withContext
            val mediaItem: MediaItem = MediaItem.fromUri(_animeUiState.value.animeStreamLink!!)
            player.setMediaItem(mediaItem)
            player.play()
            _animeUiState.update {
                it.copy(isPlaying = true)
            }
        }

    suspend fun playDownloadAnime(animeStreamUrl: String) =
        withContext(Dispatchers.Main) {
            val fileUri = Uri.fromFile(File(animeStreamUrl))
            Log.d("AnimeStreamViewModel", "playDownloadAnime: $fileUri")
            val mediaItem: MediaItem = MediaItem.fromUri(fileUri)
            player.setMediaItem(mediaItem)
            player.play()
            _animeUiState.update {
                it.copy(isPlaying = true)
            }
        }

    private fun getCustomPlayerListener(): Player.Listener {
        return object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("AnimeStreamViewModel", "onPlaybackStateChanged: $playbackState")
                if (playbackState == PlaybackState.STATE_NONE || playbackState == PlaybackState.STATE_CONNECTING || playbackState == PlaybackState.STATE_STOPPED || playbackState == PlaybackState.STATE_PAUSED) {
                    _animeUiState.update {
                        it.copy(isLoading = true)
                    }
                }
                else
                    _animeUiState.update {
                        it.copy(isLoading = false)
                    }
                super.onPlaybackStateChanged(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                val progress = player.duration - player.currentPosition
                if (progress <= 0 && !isPlaying)
                    _animeUiState.update {
                        it.copy(isNextEp = true)
                    }
            }
        }
    }

    fun checkHistoryAnime(animeLink: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.checkHistoryAnime(animeLink).apply {
                    _animeUiState.update {
                        it.copy(isInHistory = this)
                    }
                }
            }
        }
    }

    fun addHistoryAnime(anime: Anime, animeEpisode: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val animeHistory = AnimeHistory(
                    animeName = anime.animeName,
                    animeImageURL = anime.animeImageURL,
                    animeLink = anime.animeLink,
                    animeEpisode = animeEpisode
                )
                animeRepository.addHistoryAnime(animeHistory)
            }
        }
    }

    fun updateHistoryAnime(animeLink: String, animeEpisode: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.updateHistoryAnime(animeLink, animeEpisode)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

}