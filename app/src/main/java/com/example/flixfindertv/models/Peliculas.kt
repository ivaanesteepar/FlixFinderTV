package com.example.flixfindertv.models

import com.google.gson.annotations.SerializedName

data class Peliculas(
    val id: String = "", // ID de Firebase
    val title: String? = null,
    val name: String? = null,
    val overview: String = "",
    val releaseDate: String? = null,
    val release_date_series: String? = null,
    val poster_path: String = "",
    val voteAverage: String = "0.0",
    val voteCount: String = "0",
    val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    val backdropPath: String = "",
    val popularity: Double = 0.0,
    val esSerie: Boolean = false,
    val comentarios: List<String> = emptyList()
) {
    val titulo: String
        get() = title ?: name ?: "Título desconocido"
}

data class MovieResponse(
    @SerializedName("results")
    val resultados: List<Peliculas>,

    @SerializedName("total_pages")
    val totalPages: Int
)
