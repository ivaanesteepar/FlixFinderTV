package com.example.flixfindertv.models

data class TranslationRequest(
    val q: String,
    val source: String,
    val target: String
)
data class TranslationResponse(
    val translatedText: String  // Este es el campo que devuelve la respuesta de LibreTranslate
)