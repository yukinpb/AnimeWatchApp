package com.example.animewatchapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DownloadUiState(
    val animeList: MutableList<AnimeDownload> = mutableListOf(),
    val isLoading: Boolean = false,
    var searchKeyword: String = ""
)

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(DownloadUiState())
    val animeUiState: StateFlow<DownloadUiState> = _animeUiState

    init {
        getDownloadAnimeList()
    }

    private fun getDownloadAnimeList() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.getAllDownloadAnime().collect { listAnime ->
                    val newList = listAnime.toMutableList()
                    newList.reverse()
                    _animeUiState.update {
                        it.copy(animeList = newList, isLoading = false)
                    }
                }
            }
        }
    }

    fun searchDownloadAnime(animeName: String) {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.searchDownloadAnime(animeName).collect { listAnime ->
                    val newList = listAnime.toMutableList()
                    newList.reverse()
                    _animeUiState.update {
                        it.copy(animeList = newList, isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteDownloadAnime(animeLink: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.deleteDownloadAnime(animeLink)
            }
        }
    }

    fun deleteAllDownloadAnime() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.deleteAllDownloadAnime()
            }
        }
    }
}