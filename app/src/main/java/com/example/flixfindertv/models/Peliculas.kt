package com.example.flixfindertv.models

data class Peliculas(
    val id: String = "", // ID de Firebase
    val title: String? = null,
    val name: String? = null,
    val overview: String = "",
    val release_date: String? = null,
    val release_date_series: String? = null,
    val poster_path: String = "",
    val vote_average: String = "0.0",
    val vote_count: String = "0",
    val genre_ids: List<Int> = emptyList(),
    val adult: Boolean = false,
    val backdrop_path: String = "",
    val popularity: Double = 0.0,
    val esSerie: Boolean = false,
    val comentarios: List<String> = emptyList()
) {
    val titulo: String
        get() = title ?: name ?: "Título desconocido"
}
