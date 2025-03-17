package com.example.flixfindertv.network

import com.example.flixfindertv.models.GPTRequest
import com.example.flixfindertv.models.OpenAIResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

const val OPENAI_API_KEY = "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"

interface OpenAIApi {
    @Headers(
        "Authorization: Bearer $OPENAI_API_KEY",
        "Content-Type: application/json"
    )
    @POST("v1/chat/completions") // Verifica este endpoint
    suspend fun getTrivia(@Body request: GPTRequest): OpenAIResponse
}

object OpenAiRetrofitClient {
    private const val BASE_URL = "https://api.openai.com/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // OpenAI base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }
}

