package com.example.animewatchapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "history_anime")
data class AnimeHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var animeName: String = "",
    var animeImageURL: String = "",
    var animeLink: String = "",
    var animeEpisode: Int = 0,
    var lastWatched: Date = Date()
): Serializable