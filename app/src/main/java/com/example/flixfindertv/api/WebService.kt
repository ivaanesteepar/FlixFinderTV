package com.example.flixfindertv.api

import com.example.flixfindertv.models.MovieResponse
import com.example.flixfindertv.models.GenreResponse  // Asegúrate de tener esta clase para los géneros
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface WebService {

    // Peliculas populares
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int
    ): Response<MovieResponse>

    // Series populares
    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int
    ): Response<MovieResponse>

    // Géneros de películas
    @GET("genre/movie/list")
    suspend fun getMoviesGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<GenreResponse>  // Asegúrate de tener esta clase para la respuesta de géneros

    // Géneros de series
    @GET("genre/tv/list")
    suspend fun getTVShowsGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<GenreResponse>  // Igual que arriba, debes tener una clase para los géneros
}
