package com.example.flixfindertv.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.flixfindertv.models.Peliculas
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("movies_and_series_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Guardar una lista de películas
    fun saveMovies(movies: List<Peliculas>, category: String) {
        val json = gson.toJson(movies)
        sharedPreferences.edit().putString(category, json).apply()
    }

    // Obtener una lista de películas
    fun getMovies(category: String): List<Peliculas>? {
        val json = sharedPreferences.getString(category, null)
        return if (json != null) {
            val type = object : TypeToken<List<Peliculas>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    // Guardar una lista de series
    fun saveSeries(series: List<Peliculas>, category: String) {
        val json = gson.toJson(series)
        sharedPreferences.edit().putString(category, json).apply()
    }

    // Obtener una lista de series
    fun getSeries(category: String): List<Peliculas>? {
        val json = sharedPreferences.getString(category, null)
        return if (json != null) {
            val type = object : TypeToken<List<Peliculas>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
}
