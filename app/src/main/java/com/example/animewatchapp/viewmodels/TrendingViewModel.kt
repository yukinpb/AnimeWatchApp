package com.example.animewatchapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class TrendingUiState(
    val animeList: MutableList<Anime> = mutableListOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(TrendingUiState())
    val animeUiState: StateFlow<TrendingUiState> = _animeUiState

    init {
        getTrendingAnimeList()
    }

    fun getTrendingAnimeList() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    withTimeout(5000L) {
                        animeRepository.getTrendingAnime().apply {
                            if(this.isEmpty()) {
                                _animeUiState.update {
                                    it.copy(isError = true, isLoading = false)
                                }
                            }
                            else {
                                _animeUiState.update {
                                    it.copy(animeList = this, isLoading = false, isError = false)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _animeUiState.update {
                        it.copy(isError = true, isLoading = false)
                    }
                }
            }
        }
    }
}