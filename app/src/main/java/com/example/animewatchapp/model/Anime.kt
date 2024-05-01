package com.example.animewatchapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "favorite_anime")
data class Anime(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var animeName: String = "",
    var animeImageURL: String = "",
    var animeLink: String = ""
): Serializable