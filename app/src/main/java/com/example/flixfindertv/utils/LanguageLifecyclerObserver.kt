package com.example.flixfindertv.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageLifecycleObserver(private val languageState: MutableStateFlow<String>) :
    LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        println("prueba1")
        if (event == Lifecycle.Event.ON_RESUME) {
            val currentLanguage = Locale.getDefault().language

            // Usamos lifecycleScope para lanzar la corrutina
            source.lifecycleScope.launch {
                languageState.value = currentLanguage
                println("Idioma en observer: $currentLanguage")
            }
        }
        println("prueba2")
    }
}
