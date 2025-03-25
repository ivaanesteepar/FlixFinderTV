package com.example.flixfindertv.models
import com.google.firebase.Timestamp


data class Comentarios(
    val id: String = "",               // Agregar valores predeterminados para que el constructor sin parámetros sea posible
    val usuario: String = "",
    val puntuacion: Int = 0,
    val comentario: String = "",
    val respuestas: List<Respuestas> = emptyList(),
    val idContenido: String = "",       // Agregar valores predeterminados para cada campo
    val fechaPublicacion: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val nombreLikes: List<String> = emptyList()
)

data class Respuestas(
    val id: String = "",
    val idComentario: String = "",
    val idContenido: String = "",
    val usuario: String = "",
    val respuesta: String = "",
    val fechaPublicacion: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val nombreLikes: List<String> = emptyList()
)