package com.example.flixfindertv.utils

import android.util.Log
import com.example.flixfindertv.models.TranslationRequest
import com.example.flixfindertv.network.TranslateApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.targum.io/"

    val api: TranslateApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslateApiService::class.java)
    }

    suspend fun translateFunction(description: String): String {
        return try {
            // Preparar la solicitud para traducir de inglés a español
            val request = TranslationRequest(
                q = description,
                source = "en",  // Idioma de origen (inglés)
                target = "es"   // Idioma de destino (español)
            )

            // Llamar a la API de LibreTranslate
            val response = api.translate(request)

            // Imprimir la respuesta para depuración
            println("La respuesta es: ${response.translatedText}")

            // Devolver el texto traducido
            response.translatedText ?: description // Si no hay traducción, devuelve el texto original
        } catch (e: Exception) {
            // Log de error si falla la traducción
            Log.e("TranslationError", "Error al traducir: ${e.message}")
            description // Retorna el texto original en caso de error
        }
    }
}
