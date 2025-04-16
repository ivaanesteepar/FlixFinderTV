package com.example.flixfindertv.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.example.flixfindertv.interfaces.UiState
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.TriviaViewModel
import com.example.flixfindertv.ui.viewmodels.TriviaViewModelFactory
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@Composable
fun TriviaScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val triviaViewModel: TriviaViewModel = viewModel(
        factory = TriviaViewModelFactory(context, context as LifecycleOwner)
    )

    var answer by rememberSaveable { mutableStateOf("") }
    var fullText by rememberSaveable { mutableStateOf("") }
    var questionText by rememberSaveable { mutableStateOf("") }
    var answersList by rememberSaveable { mutableStateOf(listOf<String>()) }
    var result by rememberSaveable { mutableStateOf("") }
    val uiState by triviaViewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    var showNextButton by rememberSaveable { mutableStateOf(false) }
    var explanationShown by rememberSaveable { mutableStateOf(false) }
    var hasAnswered by rememberSaveable { mutableStateOf(false) }
    var isAnswered by rememberSaveable { mutableStateOf(false) }
    var transitioningToNextQuestion by rememberSaveable { mutableStateOf(false) }
    var showWelcomeScreen by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(true) {
        triviaViewModel.registerLanguageObserver(context as LifecycleOwner)
    }

    // BackHandler para manejar el retroceso
    BackHandler {
        if (!showWelcomeScreen) {
            showWelcomeScreen = true
            answer = ""
            fullText = ""
            questionText = ""
            answersList = emptyList()
            result = ""
            showNextButton = false
            explanationShown = false
            hasAnswered = false
            isAnswered = false
            transitioningToNextQuestion = false
            triviaViewModel.generateQuestion()
        } else {
            activity?.finish()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState !is UiState.Success) {
            triviaViewModel.generateQuestion()
        }

        if (!explanationShown) {
            showNextButton = false
        }
    }

    // Retraso de  segundos antes de mostrar la explicación
    LaunchedEffect(hasAnswered) {
        if (hasAnswered && !explanationShown) {
            explanationShown = true // Mostrar la explicación
        }
    }

    // Retraso antes de ir a la siguiente pregunta
    LaunchedEffect(transitioningToNextQuestion) {
        if (transitioningToNextQuestion) {
            triviaViewModel.generateQuestion()
            answer = ""
            fullText = ""
            questionText = ""
            answersList = emptyList()
            showNextButton = false
            explanationShown = false
            hasAnswered = false
            isAnswered = false
            transitioningToNextQuestion = false
            triviaViewModel.explanation = ""
            delay(1000) // 1 segundo de espera antes de mostrar la siguiente pregunta
        }
    }

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo_app),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (hayConexion) {
                    if (showWelcomeScreen) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Welcome to the FlixFinderTV Trivia!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(
                                    onClick = { showWelcomeScreen = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                ) {
                                    Text(text = "Start", color = Color.White)
                                }

                            }
                            Text(
                                text = "Powered by Gemini AI",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .offset(y = (-20).dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .padding(top = 30.dp)
                        ) {
                            if (uiState is UiState.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            } else if (uiState is UiState.Success) {
                                fullText = (uiState as UiState.Success).outputText

                                val answerStartIndex = fullText.indexOfFirst { it == 'A' }

                                if (answerStartIndex != -1) {
                                    questionText = fullText.substring(0, answerStartIndex).trim()
                                    val answersText = fullText.substring(answerStartIndex).trim()
                                    answersList =
                                        answersText.split(Regex("(?=\\s*[A-D]\\)\\s*)"))
                                            .map { it.trim() }
                                            .filter { it.isNotEmpty() }
                                }
                            } else if (uiState is UiState.Error) {
                                Text(
                                    text = (uiState as UiState.Error).errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }

                            // Mostrar la pregunta solo si no se ha respondido aún
                            if (!hasAnswered && questionText.isNotEmpty()) {
                                Text(
                                    text = questionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(50.dp))
                                // Mostrar las opciones de respuesta
                                answersList.forEachIndexed { index, answerOption ->
                                    Button(
                                        onClick = {
                                            // Aquí va la lógica para validar la respuesta
                                            val normalizedAnswer = when (index) {
                                                0 -> "a"
                                                1 -> "b"
                                                2 -> "c"
                                                3 -> "d"
                                                else -> ""
                                            }
                                            triviaViewModel.checkAnswer(normalizedAnswer)
                                            showNextButton = true
                                            hasAnswered = true
                                            isAnswered = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 15.dp)
                                    ) {
                                        Text(text = answerOption)
                                    }
                                }
                            }
                        }

                        // Mostrar la explicación después de la espera
                        if (hasAnswered && explanationShown) {
                            LazyColumn(
                                modifier = Modifier.height(530.dp)
                            ) {
                                item {
                                    // Usar la variable global 'explanation' para mostrar la explicación
                                    Text(
                                        text = triviaViewModel.explanation ?: "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(26.dp),
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Fila con los botones fijos en la parte inferior
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            // Botón para continuar a la siguiente pregunta
                            if (showNextButton) {
                                Button(
                                    onClick = {
                                        // Animar el cambio de la explicación a la siguiente pregunta
                                        transitioningToNextQuestion = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Next question")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "You need an internet connection to play",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

