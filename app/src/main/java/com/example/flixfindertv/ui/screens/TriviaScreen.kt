package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.interfaces.UiState
import com.example.flixfindertv.ui.viewmodels.TriviaViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TriviaScreen(
    navController: NavHostController,
    triviaViewModel: TriviaViewModel = viewModel()
) {
    var answer by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    val uiState by triviaViewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Estado para mostrar el botón de siguiente pregunta
    var showNextButton by rememberSaveable { mutableStateOf(false) }

    // Estado para controlar si ya se mostró la explicación
    var explanationShown by rememberSaveable { mutableStateOf(false) }

    // Estado para saber si se ha enviado una respuesta
    var hasAnswered by rememberSaveable { mutableStateOf(false) }

    // Genera la pregunta automáticamente cuando se entra en la pantalla
    LaunchedEffect(Unit) {
        if (uiState !is UiState.Success) {  // Solo generar si no hay una pregunta guardada
            triviaViewModel.generateQuestion()
        }

        // Si ya se ha mostrado la explicación, no restablecer el botón
        if (!explanationShown) {
            showNextButton = false  // Asegúrate de ocultar el botón al principio
        }
    }

    println("ExplanationShown: $explanationShown")

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mostramos la pregunta solo si está disponible
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState is UiState.Success) {
                    val question = (uiState as UiState.Success).outputText
                    Text(
                        text = question,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)  // Deja que la pregunta ocupe el espacio restante
                            .padding(bottom = 16.dp)
                            .verticalScroll(rememberScrollState())  // Habilita desplazamiento solo para la pregunta
                    )
                } else if (uiState is UiState.Error) {
                    Text(
                        text = (uiState as UiState.Error).errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Campo de texto y botones fijos en la parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth() // Asegúrate de que la columna ocupe el ancho completo
            ) {
                // Campo de texto para la respuesta
                TextField(
                    value = answer,
                    label = { Text("Write your response") },
                    onValueChange = { answer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .heightIn(min = 56.dp) // Controlar la altura mínima del campo de texto
                )

                // Botón para enviar respuesta
                Button(
                    onClick = {
                        val normalizedAnswer = answer.lowercase().trim() // Convertir a minúsculas y eliminar espacios
                        if (normalizedAnswer in listOf("a", "b", "c", "d")) {
                            triviaViewModel.checkAnswer(normalizedAnswer)
                            showNextButton = true  // Muestra el botón de siguiente pregunta después de responder
                            answer = "" // Limpia el campo de respuesta
                            hasAnswered = true // Marca que ya se ha respondido
                        } else {
                            result = "Invalid answer. Please enter a, b, c, or d."
                        }
                    },
                    enabled = answer.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(text = "Send response")
                }

                // Mostrar mensaje de error si la respuesta es inválida
                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Mostrar el resultado de la respuesta solo si se ha respondido
                if (hasAnswered) {
                    explanationShown = true
                }

                // Botón de siguiente pregunta
                if (showNextButton) {
                    Button(
                        onClick = {
                            triviaViewModel.generateQuestion()  // Genera una nueva pregunta
                            showNextButton = false  // Oculta el botón de siguiente pregunta
                            result = ""  // Limpia el resultado anterior
                            explanationShown = false // Resetear el estado de explicación mostrada
                            hasAnswered = false  // Resetear el estado de respuesta
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Next question")
                    }
                }
            }
        }
    }
}
