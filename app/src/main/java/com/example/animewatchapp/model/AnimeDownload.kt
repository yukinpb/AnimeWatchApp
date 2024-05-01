package com.example.animewatchapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "download_anime")
data class AnimeDownload(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var animeName: String = "",
    var animeImageURL: String = "",
    var animeLink: String = "",
    var animeEpisode: Int = 0
): Serializable