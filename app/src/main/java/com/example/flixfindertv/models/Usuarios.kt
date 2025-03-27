package com.example.flixfindertv.models

data class Usuarios (
    val nombre: String = "",
    val email: String = "",
    val uid: String = "",
    val fotoPerfil: String? = null,
    val fechaNacimiento: String = "",
    val siguiendo: List<String> = emptyList(),
    val seguidores: List<String> = emptyList(),
    val numComentarios: Int = 0,
    val peliculasFavoritas: List<Map<String, Any>> = emptyList(),
    val seriesFavoritas: List<Map<String, Any>> = emptyList(),
    val contenidoVisto: String = "", // para recomendaciones (solo se almacena 1 pelicula/serie)
    val generosFavoritos: Map<String, Long> = emptyMap(),
    val esNuevo: Boolean = true,
    val admin: Boolean = false
)