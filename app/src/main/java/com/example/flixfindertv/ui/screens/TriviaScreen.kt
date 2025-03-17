package com.example.flixfindertv.ui.screens

import com.example.flixfindertv.ui.viewmodels.TriviaViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

@Composable
fun TriviaScreen(navController: NavHostController) {
    val triviaViewModel: TriviaViewModel = viewModel()
    val firebaseAuth = FirebaseAuth.getInstance()
    val uid = firebaseAuth.currentUser?.uid

    var userAnswer by remember { mutableStateOf("") }
    var isAnswerChecked by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isInvalidAnswer by remember { mutableStateOf(false) }

    // Obtener la respuesta de GPT cuando la pantalla se lanza
    LaunchedEffect(true) {
        triviaViewModel.getGPTResponse()
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Cuadro de texto para la respuesta del usuario
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(8.dp)
                ) {
                    // Placeholder, visible cuando no hay texto
                    if (userAnswer.isEmpty()) {
                        Text(
                            text = "Responde aquí...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }

                    // BasicTextField para la entrada del usuario
                    BasicTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }



                Spacer(modifier = Modifier.height(8.dp))

                // Botón para enviar la respuesta
                Button(
                    onClick = {
                        if (userAnswer.trim().isEmpty()) {
                            isInvalidAnswer = true
                            isAnswerChecked = false
                        } else {
                            isAnswerChecked = true
                            isCorrect = triviaViewModel.checkAnswer(userAnswer)
                            isInvalidAnswer = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Responder")
                }

                // Si el usuario está autenticado, muestra el BottomNavigationBar
                if (uid != null) {
                    BottomNavigationBar(
                        navController = navController,
                        uid = uid
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                if (isLoading) {
                    // Mostrar el progreso mientras se obtiene la respuesta de GPT
                    CircularProgressIndicator()
                } else {
                    // Mostrar la pregunta recibida de GPT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = triviaViewModel.question.value,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar las opciones de respuesta
                    triviaViewModel.options.value.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("• $option", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                // Mostrar si la respuesta ha sido verificada
                if (isAnswerChecked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isCorrect) "✅ ¡Correcto!" else "❌ Incorrecto. La respuesta correcta es: ${triviaViewModel.correctAnswer.value}",
                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                // Mostrar mensaje de error si la respuesta es inválida
                if (isInvalidAnswer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "❌ Respuesta inválida. Por favor, elige una de las opciones.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

