package com.example.flixfindertv.models

import androidx.room.PrimaryKey

// Clase que representa una película o serie con sus datos
data class Peliculas(
    @PrimaryKey val id: String = "",
    val title: String? = null,
    val name: String? = null,
    val overview: String = "",
    val release_date: String? = null,
    val release_date_series: String? = null,
    val poster_path: String? = null,
    val vote_average: String = "0.0",
    val vote_count: String = "0",
    val genre_ids: List<Int> = emptyList(),
    val adult: Boolean = false,
    val backdrop_path: String? = null,
    val popularity: Double = 0.0,
    val esSerie: Boolean = false,
    val comentarios: List<String> = emptyList(),
    val original_language: String = "",
    val status: String = "",
    val trailer: String? = null,
    val director_name: String? = "",
    val director_photo_url: String? = "",
    val seasons: Int? = null,
    val duration: Int? = null
) {
    val titulo: String
        get() = title ?: name ?: "Título desconocido"
}
