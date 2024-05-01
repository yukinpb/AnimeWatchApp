package com.example.animewatchapp.model

data class AnimeDetails(
    val animeName: String,
    val animeDesc: String,
    val animeCover: String,
    val animeEpisodes: List<Int>
)