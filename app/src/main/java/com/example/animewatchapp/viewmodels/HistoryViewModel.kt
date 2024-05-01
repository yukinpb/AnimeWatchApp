package com.example.animewatchapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animewatchapp.model.AnimeHistory
import com.example.animewatchapp.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HistoryUiState(
    val animeList: MutableList<AnimeHistory> = mutableListOf(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(HistoryUiState())
    val animeUiState: StateFlow<HistoryUiState> = _animeUiState

    init {
        getHistoryAnimeList()
    }

    private fun getHistoryAnimeList() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.getAllHistoryAnime().collect { listHistoryAnime ->
                    Log.d("HistoryViewModel", "getHistoryAnimeList: ${listHistoryAnime.size}")
                    _animeUiState.update {uiState ->
                        uiState.copy(animeList = listHistoryAnime.sortedByDescending { it.lastWatched }.toMutableList(), isLoading = false)
                    }
                }
            }
        }
    }
}