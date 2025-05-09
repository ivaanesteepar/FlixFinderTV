package com.example.flixfindertv.utils

import android.util.Log
import com.example.flixfindertv.BuildConfig
import com.example.flixfindertv.models.ImgurResponse
import com.example.flixfindertv.network.ImgurApiService
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


object ImgurUploader {
    private const val BASE_URL = "https://api.imgur.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private val apiService: ImgurApiService = retrofit.create()

    // Función no suspendida, usamos withContext
    fun uploadImage(
        imageBytes: ByteArray,
        callback: (String?) -> Unit
    ) {
        val clientId = BuildConfig.imgur_client_id

        if (clientId.isNullOrEmpty()) {
            Log.e("ImgurUploader", "Client ID no encontrado en BuildConfig")
            callback(null)
            return
        }

        val requestFile = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile)

        // Usar el clientId dinámico obtenido del ViewModel
        val call = apiService.uploadImage("Client-ID $clientId", imagePart)
        call.enqueue(object : retrofit2.Callback<ImgurResponse> {
            override fun onResponse(
                call: retrofit2.Call<ImgurResponse>,
                response: retrofit2.Response<ImgurResponse>
            ) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.data?.link
                    callback(imageUrl)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: retrofit2.Call<ImgurResponse>, t: Throwable) {
                Log.e("ImgurUploader", "Error subiendo imagen", t)
                callback(null)
            }
        })
    }
}