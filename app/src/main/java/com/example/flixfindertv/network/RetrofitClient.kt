package com.example.flixfindertv.network

import com.example.flixfindertv.api.WebService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)  // Timeout de conexión
        .readTimeout(5, TimeUnit.MINUTES)     // Timeout de lectura
        .writeTimeout(5, TimeUnit.MINUTES)    // Timeout de escritura
        .build()

    val webService: WebService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)  // Asignamos el cliente con los timeouts (tiene 5 minutos para restablecer la conexión)
            .build()
            .create(WebService::class.java)
    }
}
