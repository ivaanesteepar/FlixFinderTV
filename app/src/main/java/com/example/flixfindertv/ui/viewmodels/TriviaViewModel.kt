package com.example.flixfindertv.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.GPTRequest
import com.example.flixfindertv.models.Message
import com.example.flixfindertv.models.OpenAIResponse
import com.example.flixfindertv.network.OpenAiRetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TriviaViewModel : ViewModel() {

    val gptResponse = mutableStateOf("")
    val question = mutableStateOf("")
    val options = mutableStateOf(listOf<String>())
    val correctAnswer = mutableStateOf("")

    // Función para obtener la respuesta de GPT (pregunta sobre películas y series con opciones)
    fun getGPTResponse() {
        viewModelScope.launch {
            try {
                // Crear el request con el prompt específico para generar preguntas sobre películas y series
                val request = GPTRequest(
                    messages = listOf(
                        Message(
                            role = "user",
                            content = "Genera una pregunta sobre películas o series con cuatro opciones de respuesta. Luego, proporciona una línea diciendo... La respuesta correcta es: (A, B, C o D)."
                        )
                    )
                )

                // Realizar la solicitud usando Retrofit
                val response: OpenAIResponse = OpenAiRetrofitClient.api.getTrivia(request)
                println("Respuesta de OpenAI recibida: ${response.choices[0].message.content}") // Mostrar la respuesta de GPT

                // Extraer la respuesta de GPT (se espera que sea una pregunta con 4 opciones y una línea con la letra de la respuesta correcta)
                val responseContent = response.choices[0].message.content
                gptResponse.value = responseContent

                val splitContent = responseContent.split("\n")

                // Procesar la pregunta y las opciones
                if (splitContent.size >= 6) {
                    question.value = splitContent[0] // La primera línea es la pregunta
                    options.value = splitContent.drop(1).take(4) // Las siguientes líneas son las opciones
                    println("Pregunta: ${question.value}") // Imprimir la pregunta
                    println("Opciones: ${options.value.joinToString()}")

                    // La última línea contiene la respuesta correcta como una letra (A, B, C, D)
                    val correctOptionLetter = splitContent.last().split(":")[1].trim() // "A"
                    correctAnswer.value = correctOptionLetter
                    println("Respuesta correcta: ${correctAnswer.value}")
                }

            } catch (e: HttpException) {
                // Imprimir detalles más completos del error HTTP
                val response = e.response() // Obtener la respuesta completa de la excepción
                val errorMessage = response?.errorBody()?.string() // Extraer el cuerpo del error
                println("Error HTTP: ${response?.code()}") // Código de estado
                println("Mensaje de error: ${e.message()}") // Mensaje de error
                println("Cuerpo de la respuesta de error: $errorMessage") // Cuerpo de la respuesta de error

                gptResponse.value = "Error en la solicitud HTTP: ${e.message()}"
                e.printStackTrace() // Esto imprimirá la traza completa del error
            } catch (e: Exception) {
                // Captura otros errores generales
                println("Error general: ${e.localizedMessage}")
                gptResponse.value = "Error: ${e.localizedMessage}"
                e.printStackTrace() // Esto imprimirá la traza completa del error
            }
        }
    }

    // Función para comprobar si la respuesta del usuario es correcta
    fun checkAnswer(userAnswer: String): Boolean {
        return userAnswer.uppercase() == correctAnswer.value.uppercase()
    }
}
