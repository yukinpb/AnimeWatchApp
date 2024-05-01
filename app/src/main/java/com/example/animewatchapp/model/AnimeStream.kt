package com.example.animewatchapp.model

import java.io.Serializable

data class AnimeStream(
    var animeName: String = "",
    var animeImageURL: String = "",
    var animeLink: String = "",
    var animeEpisode: Int = 0,
    var animeEpisodes: MutableList<Int> = mutableListOf(),
): Serializable