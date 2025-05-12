package com.example.flixfindertv.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.example.flixfindertv.interfaces.UiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class TriviaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `generateQuestion deberia emitir UiState Success cuando Gemini devuelve una pregunta`() = runTest {
        // Configurar el mock para GenerativeModel
        mockkConstructor(GenerativeModel::class)

        // Crear un mock de la respuesta
        val fakeResponse = mockk<GenerateContentResponse>()
        every { fakeResponse.text } returns "¿Cuál es la capital de Francia?\nA) París\nB) Madrid\nC) Roma\nD) Berlín"

        // Configurar la llamada de generateContent() para devolver la respuesta fake
        coEvery { anyConstructed<GenerativeModel>().generateContent(ofType(Content::class)) } returns fakeResponse

        // Crear un contexto y un ciclo de vida mockeados
        val context = mockk<Context>(relaxed = true)
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

        // Crear el ViewModel
        val viewModel = TriviaViewModel(context, lifecycleOwner)

        // Llamar a la función que genera la pregunta
        viewModel.generateQuestion()

        // Adelantar el tiempo para que las coroutines se ejecuten
        advanceUntilIdle()

        // Esperar a que el estado cambie a UiState.Success
        var result = viewModel.uiState.value
        while (result !is UiState.Success) {
            // Avanzar el tiempo nuevamente y obtener el nuevo estado
            advanceUntilIdle()
            result = viewModel.uiState.value
        }

        // Verificar que el estado no es nulo y es del tipo esperado
        assertNotNull(result)

        // Verificar que el estado es del tipo UiState.Success
        assertTrue(result is UiState.Success)

        // Verificar que el texto de la pregunta es el esperado
        val successResult = result
        assertEquals(
            "¿Cuál es la capital de Francia?\nA) París\nB) Madrid\nC) Roma\nD) Berlín",
            successResult.outputText
        )
    }

//    @Test
//    fun `checkAnswer deberia emitir UiState Success con explicacion cuando la respuesta es comprobada`() = runTest {
//        // Configurar el mock para GenerativeModel
//        mockkConstructor(GenerativeModel::class)
//
//        // Crear un mock de la respuesta
//        val fakeResponse = mockk<GenerateContentResponse>()
//        every { fakeResponse.text } returns "La respuesta correcta es A) París porque es la capital de Francia."
//
//        // Configurar la llamada de generateContent() para devolver la respuesta fake
//        coEvery { anyConstructed<GenerativeModel>().generateContent(ofType(Content::class)) } returns fakeResponse
//
//        // Crear el contexto y el ciclo de vida mockeados
//        val context = mockk<Context>(relaxed = true)
//        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)
//
//        // Crear el ViewModel
//        val viewModel = TriviaViewModel(context, lifecycleOwner)
//
//        // Configurar un estado de éxito con una pregunta falsa
//        val fakeQuestion = "¿Cuál es la capital de Francia?\nA) París\nB) Madrid\nC) Roma\nD) Berlín"
//        viewModel._uiState.value = UiState.Success(fakeQuestion)
//
//        // Llamar a la función checkAnswer con una respuesta
//        viewModel.checkAnswer("A")
//
//        // Adelantar el tiempo para que las coroutines se ejecuten
//        advanceUntilIdle()
//
//        // Verificar que el estado cambió a UiState.Success con la explicación
//        var result = viewModel.uiState.value
//        while (result !is UiState.Success) {
//            advanceUntilIdle()
//            result = viewModel.uiState.value
//        }
//
//        // Verificar que el resultado no sea nulo y sea del tipo esperado
//        assertNotNull(result)
//
//        // Verificar que el estado es del tipo UiState.Success
//        assertTrue(result is UiState.Success)
//
//        // Verificar que la explicación contiene la respuesta correcta
//        val successResult = result
//        assertTrue(successResult.outputText.contains("La respuesta correcta es A) París"))
//    }
}

