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
    val portada: String,

    @SerializedName("vote_average")
    val votoPromedio: String,

    @SerializedName("vote_count")
    val numVotos: String,

    @SerializedName("genre_ids")
    val generos: List<String>,

    @SerializedName("adult")
    val esAdulto: Boolean,

    @SerializedName("backdrop_path")
    val banner: String,

    @SerializedName("popularity")
    val popularidad: Double,

    val comentarios: List<Comentario> = listOf()


) {
    constructor() : this(
        id = "",
        tituloOriginal = null,
        nombreAlternativo = null,
        descripcion = "",
        fecha = null,
        portada = "",
        votoPromedio = "0.0",
        numVotos = "0",
        generos = listOf(),
        esAdulto = false,
        banner = "",
        popularidad = 0.0,
        comentarios = listOf()
    )

    val titulo: String
        get() = tituloOriginal ?: nombreAlternativo ?: "Título desconocido"
}


data class MovieResponse(
    @SerializedName("results")
    val resultados: List<Peliculas>,

    @SerializedName("total_pages")
    val totalPages: Int
)
