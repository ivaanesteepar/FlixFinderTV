package com.example.flixfindertv.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.BuildConfig
import com.example.flixfindertv.interfaces.UiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class TriviaViewModel() : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val apiKey = BuildConfig.apiKey

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private fun getPrompt(): String {
        val language = Locale.getDefault().language
        return if (language == "es") {
            // Prompt en español
            """
                Hazme una pregunta sobre películas o series de TV con 4 opciones de respuesta.
                Asegúrate de que la pregunta sea única y no repetida.
                No uses markdown. La pregunta debe ser atractiva y relevante.
            """.trimIndent()
        } else {
            // Prompt en inglés
            """
                Ask me a question about movies or TV shows with 4 answer options.
                Ensure the question is unique and not repeated.
                Do not use markdown. The question should be engaging and relevant.
            """.trimIndent()
        }
    }

    fun generateQuestion() {
        _uiState.value = UiState.Loading
        val prompt = getPrompt()

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
                    val evaluation = generativeModel.generateContent(content {
                        text("$currentQuestion Your answer is: $answer. What is the correct answer and why? Explain why the correct answer is what it is, in a separate paragraph. Do not use markdown.")
                    })
                    evaluation.text?.let { result ->
                        _uiState.value = UiState.Success(result)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "")
                }
            }
        }
    }
}
