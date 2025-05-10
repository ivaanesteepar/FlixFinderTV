package com.example.flixfindertv.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.BuildConfig
import com.example.flixfindertv.interfaces.UiState
import com.example.flixfindertv.utils.LanguageLifecycleObserver
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


class TriviaViewModelFactory(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificar que la clase es TriviaViewModel
        if (modelClass.isAssignableFrom(TriviaViewModel::class.java)) {
            return TriviaViewModel(context, lifecycleOwner) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class TriviaViewModel(private val context: Context, lifecycleOwner: LifecycleOwner) : ViewModel() {
    val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _languageState: MutableStateFlow<String> = MutableStateFlow(Locale.getDefault().language)
    private val languageState: StateFlow<String> = _languageState.asStateFlow()

    private val apiKey = BuildConfig.GEMINI_API_KEY
    var explanation: String? = null

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    init {
        // Aseguramos que el context sea un LifecycleOwner
        if (lifecycleOwner is LifecycleOwner) {
            val lifecycleObserver = LanguageLifecycleObserver(_languageState)
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            println("LanguageLifecycleObserver registrado correctamente")
        } else {
            println("El contexto no es un LifecycleOwner, no se pudo registrar el observer")
        }

        // Regenerar la pregunta cuando el idioma cambia
        languageState.onEach { language ->
            println("Idioma actual en el ViewModel: $language")
            generateQuestion()
        }.launchIn(viewModelScope)
    }

    // Cuando la actividad/fragmento esté activo, re-registrar el observer
    fun registerLanguageObserver(lifecycleOwner: LifecycleOwner) {
        val lifecycleObserver = LanguageLifecycleObserver(_languageState)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    private fun getPrompt(): String {
        val language = _languageState.value
        return if (language == "es") {
            // Prompt en español con 4 opciones
            """
            Hazme una pregunta sobre películas o series de TV con 4 opciones de respuesta.
            Asegúrate de que la pregunta sea única y no repetida.
            No uses markdown. La pregunta debe ser atractiva y relevante.

            Ejemplo de pregunta con opciones:
            ¿Cuál es la película más taquillera de todos los tiempos?
            A) Titanic
            B) Avatar
            C) Avengers: Endgame
            D) Star Wars: The Force Awakens
        """.trimIndent()
        } else {
            // Prompt en inglés con 4 opciones
            """
            Ask me a question about movies or TV shows with 4 answer options.
            Ensure the question is unique and not repeated.
            Do not use markdown. The question should be engaging and relevant.

            Example question with options:
            What is the highest-grossing movie of all time?
            A) Titanic
            B) Avatar
            C) Avengers: Endgame
            D) Star Wars: The Force Awakens
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
                    // Obtener el idioma actual del dispositivo
                    val language = _languageState.value

                    // Generar el contenido en base al idioma
                    val evaluation = generativeModel.generateContent(content {
                        if (language == "es") {
                            text("$currentQuestion Tu respuesta es: $answer. ¿Cuál es la respuesta correcta y por qué? Explica por qué la respuesta correcta es lo que es, en un párrafo separado. No uses markdown.")
                        } else {
                            text("$currentQuestion Your answer is: $answer. What is the correct answer and why? Explain why the correct answer is what it is, in a separate paragraph. Do not use markdown.")
                        }
                    })

                    // Asignar el resultado a la variable global 'explanation'
                    evaluation.text?.let { result ->
                        explanation = result
                    }

                    // Luego puedes actualizar la UI con la explicación si lo deseas
                    explanation?.let {
                        // Aquí puedes actualizar el estado de la UI con la explicación
                        _uiState.value = UiState.Success(it)
                    }

                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "")
                }
            }
        }
    }
}