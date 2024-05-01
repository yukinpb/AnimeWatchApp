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

data class LatestUiState(
    val currentPage: Int = 1,
    val animeList: MutableList<Anime> = mutableListOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

@HiltViewModel
class LatestViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(LatestUiState())
    val animeUiState: StateFlow<LatestUiState> = _animeUiState

    init {
        getLatestAnimeList()
    }

    fun getLatestAnimeList() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        animeRepository.getLatestAnime(_animeUiState.value.currentPage).apply {
                            if(this.isEmpty()) {
                                _animeUiState.update {
                                    it.copy(isError = true, isLoading = false)
                                }
                            }
                            else {
                                val newList = _animeUiState.value.animeList
                                newList.addAll(this)
                                _animeUiState.update {
                                    it.copy(animeList = newList, isLoading = false, currentPage = it.currentPage + 1, isError = false)
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
}

