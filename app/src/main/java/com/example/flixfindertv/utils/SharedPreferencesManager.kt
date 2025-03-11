package com.example.flixfindertv.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.flixfindertv.models.Peliculas
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("flixfinder_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    // Guardar listas de películas y series
    fun saveMoviesAndSeries(movies: List<Peliculas>, series: List<Peliculas>) {
        val editor = sharedPreferences.edit()
        editor.putString("movies", gson.toJson(movies))
        editor.putString("series", gson.toJson(series))
        editor.apply()
    }

    // Cargar listas de películas y series
    fun loadMoviesAndSeries(): Pair<List<Peliculas>, List<Peliculas>> {
        val moviesJson = sharedPreferences.getString("movies", "[]")
        val seriesJson = sharedPreferences.getString("series", "[]")

        val movieType = object : TypeToken<List<Peliculas>>() {}.type
        val serieType = object : TypeToken<List<Peliculas>>() {}.type

        val movies = gson.fromJson<List<Peliculas>>(moviesJson, movieType)
        val series = gson.fromJson<List<Peliculas>>(seriesJson, serieType)

        return Pair(movies, series)
    }

    // Borrar las películas y series almacenadas
    fun clearMoviesAndSeries() {
        val editor = sharedPreferences.edit()
        editor.remove("movies")
        editor.remove("series")
        editor.apply()
    }
}
