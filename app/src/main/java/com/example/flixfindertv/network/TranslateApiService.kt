package com.example.flixfindertv.network

import com.example.flixfindertv.models.TranslationRequest
import com.example.flixfindertv.models.TranslationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslateApiService {
    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse
}
