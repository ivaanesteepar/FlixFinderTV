package com.example.flixfindertv.models

data class Usuarios (
    val nombre: String = "",
    val email: String = "",
    val uid: String = "",
    val fotoPerfil: String = "",
    val fechaNacimiento: String = "",
    val siguiendo: List<String> = emptyList(),
    val seguidores: List<String> = emptyList(),
    val numComentarios: Int = 0,
    val seriesFavoritas: List<String> = emptyList(),
    val peliculasFavoritas: List<String> = emptyList(),
    val contenidoVisto: String = "", // para recomendaciones (solo se almacena 1 pelicula/serie)
    val generosFavoritos: Map<String, Long> = emptyMap(),
    val esNuevo: Boolean = true
)