package com.ivaanesteepar.flixfindertv.models

import com.google.firebase.Timestamp

// Clase que representa un comentario hecho por un usuario sobre un contenido (película o serie)
data class Comentarios(
    val id: String = "",
    val usuario: String = "",
    val puntuacion: Int = 0,
    val comentario: String = "",
    var respuestas: List<Respuestas> = emptyList(),
    val idContenido: String = "",
    val fechaPublicacion: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val nombreLikes: List<String> = emptyList(),
    var revision: Boolean = false
)

// Clase que representa una respuesta a un comentario dentro de la sección de comentarios
data class Respuestas(
    val id: String = "",
    val idComentario: String = "",
    val idContenido: String = "",
    val usuario: String = "",
    val respuesta: String = "",
    val fechaPublicacion: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val nombreLikes: List<String> = emptyList(),
    var revision: Boolean = false
)
