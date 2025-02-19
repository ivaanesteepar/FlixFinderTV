package com.example.flixfindertv.models

data class Usuarios (
    val nombre: String = "",
    val email: String = "",
    val uid: String = "",
    val fotoPerfil: String = "",
    val contenidoVisto: List<String> = emptyList(),
    val generosFavoritos: List<String> = emptyList()
)