package com.example.flixfindertv.models

import java.util.Date

data class Peliculas(
    val fecha: Date,
    val titulo: String,
    val descripcion: String,
    val numVotos: Int,
    val mediaVotos: Double,
    val idioma: String,
    val genero: String,
    val imagen: String
)