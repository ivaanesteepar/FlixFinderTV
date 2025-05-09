package com.example.flixfindertv.models

// Clase que representa la respuesta completa de la API de Imgur tras subir una imagen
data class ImgurResponse(
    val data: ImgurData,
    val success: Boolean,
    val status: Int
)

// Clase que contiene los datos relevantes devueltos por Imgur, como el enlace de la imagen subida
data class ImgurData(
    val link: String
)
