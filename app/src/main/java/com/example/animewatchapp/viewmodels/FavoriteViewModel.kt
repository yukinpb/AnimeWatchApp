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
import javax.inject.Inject

data class FavoriteUiState(
    val animeList: MutableList<Anime> = mutableListOf(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
): ViewModel() {
    private val _animeUiState = MutableStateFlow(FavoriteUiState())
    val animeUiState: StateFlow<FavoriteUiState> = _animeUiState

    init {
        getFavoriteAnimeList()
    }

    private fun getFavoriteAnimeList() {
        _animeUiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                animeRepository.getAllFavoriteAnime().collect { listFavAnime ->
                    _animeUiState.update {
                        it.copy(animeList = listFavAnime.toMutableList(), isLoading = false)
                    }
                }
            }
        }
    }
}