package com.ivaanesteepar.flixfindertv.network

import com.ivaanesteepar.flixfindertv.models.ImgurResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// Interfaz que define el endpoint de la API de Imgur, incluyendo la función para subir una imagen
interface ImgurApiService {
    @Multipart
    @POST("3/upload")
    fun uploadImage(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    ): Call<ImgurResponse>
}
