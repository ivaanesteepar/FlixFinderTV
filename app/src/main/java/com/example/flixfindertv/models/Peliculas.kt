package com.example.flixfindertv.models

import com.google.gson.annotations.SerializedName

data class Peliculas (
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val tituloOriginal: String? = null,

    @SerializedName("name")
    val nombreAlternativo: String? = null,

    @SerializedName("overview")
    val descripcion: String,

    @SerializedName("release_date")
    val fecha: String? = null,

    @SerializedName("poster_path")
    val imagen: String,

    @SerializedName("vote_average")
    val votoPromedio: String,

    @SerializedName("vote_count")
    val votos: String
) {
    val titulo: String
        get() = tituloOriginal ?: nombreAlternativo ?: "Título desconocido"
}


data class MovieResponse(
    @SerializedName("results")
    val resultados: List<Peliculas>
)