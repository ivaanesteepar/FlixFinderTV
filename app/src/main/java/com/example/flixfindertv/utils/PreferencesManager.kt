package com.example.flixfindertv.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("flixfinder_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SHOULD_FETCH = "should_fetch"
    }

    fun setShouldFetch(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SHOULD_FETCH, value).apply()
    }

    fun getShouldFetch(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOULD_FETCH, true)
    }
}
