package com.ivaanesteepar.flixfindertv.utils

import com.ivaanesteepar.flixfindertv.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Inicializar el modelo correctamente
private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY
)

suspend fun validateComment(comment: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val prompt = "Responde solo con 'true' o 'false'. ¿Este comentario contiene palabras ofensivas o malsonantes?: \"$comment\""

            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim()?.lowercase() ?: "false"

            println("Gemini Response: $result") // Debug

            result == "true"
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error en la API o respuesta inválida")
            false // En caso de error, permitir el comentario
        }
    }
}
