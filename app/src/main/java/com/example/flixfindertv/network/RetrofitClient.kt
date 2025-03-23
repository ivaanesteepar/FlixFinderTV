package com.example.flixfindertv.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)  // Timeout de conexión
        .readTimeout(5, TimeUnit.MINUTES)     // Timeout de lectura
        .writeTimeout(5, TimeUnit.MINUTES)    // Timeout de escritura
        .build()

    // Configuración de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // Usar el OkHttpClient configurado
            .addConverterFactory(GsonConverterFactory.create())  // Conversor Gson para convertir JSON a objetos
            .build()
    }

    // Crear la instancia de la API
    val api: TMDBApiService by lazy {
        retrofit.create(TMDBApiService::class.java)
    }
}
