package com.example.flixfindertv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.interfaces.UiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class TriviaViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyAHR1-WLxXl3sbcABH-vPyLJT4nnBfHcDk"
    )

    fun generateQuestion() {
        _uiState.value = UiState.Loading
        val prompt = "Hazme una pregunta sobre películas o series con 4 opciones de respuesta."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(content { text(prompt) })
                response.text?.let { question ->
                    _uiState.value = UiState.Success(question)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun checkAnswer(answer: String) {
        val currentQuestion = (uiState.value as? UiState.Success)?.outputText
        if (currentQuestion != null) {
            _uiState.value = UiState.Loading
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Modificación: incluir "Tu respuesta es: [respuesta]" antes de la respuesta correcta
                    val evaluation = generativeModel.generateContent(content {
                        text("$currentQuestion Tu respuesta es: $answer. ¿Cuál es la respuesta correcta y por qué? Explica por qué la respuesta correcta es la que es, en un párrafo separado. No uses markdown")
                    })
                    evaluation.text?.let { result ->
                        // Aquí se espera que el resultado contenga primero la respuesta del usuario
                        _uiState.value = UiState.Success(result)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "")
                }
            }
        }
    }
}

