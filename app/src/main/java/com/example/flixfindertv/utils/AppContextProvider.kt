package com.example.flixfindertv.utils

import android.content.Context

object AppContextProvider {
    lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
