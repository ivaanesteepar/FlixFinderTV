package com.example.flixfindertv.models

import com.google.gson.annotations.SerializedName

data class Peliculas (
    @SerializedName("id")
    val id: String,
    @SerializedName("original_title")
    val titulo: String,
    @SerializedName("overview")
    val descripcion: String,
    @SerializedName("release_date")
    val fecha: String,
    @SerializedName("poster_path")
    val imagen: String,
    @SerializedName("vote_average")
    val votoPromedio: String,
    @SerializedName("vote_count")
    val votos: String

)

data class MovieResponse(
    @SerializedName("results")
    val resultados: List<Peliculas>
)