package com.example.animewatchapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animewatchapp.model.Anime
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

data class SearchUiState(
    var searchKeyword: String = "",
    val animeList: MutableList<Anime> = mutableListOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

@HiltViewModel
class SearchAnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(SearchUiState())
    val animeUiState: StateFlow<SearchUiState> = _animeUiState

    fun searchAnime() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        try {
            viewModelScope.launch {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        animeRepository.searchAnime(_animeUiState.value.searchKeyword).apply {
                            _animeUiState.update {
                                it.copy(animeList = this, isLoading = false, isError = false)
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