package com.example.flixfindertv.api

import com.example.flixfindertv.models.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface WebService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int
    ): Response<MovieResponse>

    // Series populares
    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int
    ): Response<MovieResponse>

}
