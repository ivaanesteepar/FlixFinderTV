package com.example.flixfindertv.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("flixfinder_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALMACENAR_PELICULAS = "should_fetch"
        private const val KEY_CARGAR_GENEROS = "cargarGeneros"
    }

    // Métodos para almacenar peliculas/series en Firebase solamente 1 vez
    fun setShouldFetch(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ALMACENAR_PELICULAS, value).apply()
    }

    fun getShouldFetch(): Boolean {
        return sharedPreferences.getBoolean(KEY_ALMACENAR_PELICULAS, true)
    }

    // Métodos para almacenar los géneros en Firebase solamente 1 vez
    fun setCargarGeneros(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_CARGAR_GENEROS, value).apply()
    }

    fun getCargarGeneros(): Boolean {
        return sharedPreferences.getBoolean(KEY_CARGAR_GENEROS, true)
    }
}
