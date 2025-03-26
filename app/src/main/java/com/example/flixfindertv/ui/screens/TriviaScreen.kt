package com.example.flixfindertv.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.interfaces.UiState
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

    var showNextButton by rememberSaveable { mutableStateOf(false) }
    var explanationShown by rememberSaveable { mutableStateOf(false) }
    var hasAnswered by rememberSaveable { mutableStateOf(false) }
    var isAnswered by rememberSaveable { mutableStateOf(false) }
    var transitioningToNextQuestion by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(true) {
        triviaViewModel.registerLanguageObserver(context as LifecycleOwner)
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
            delay(1000) // 2 segundos de espera
            result = ""
            explanationShown = true // Mostrar la explicación después del retraso
        }
    }

    // Retraso antes de ir a la siguiente pregunta
    LaunchedEffect(transitioningToNextQuestion) {
        if (transitioningToNextQuestion) {
            delay(1000) // 1 segundo de espera antes de mostrar la siguiente pregunta
            showNextButton = false
            explanationShown = false
            hasAnswered = false
            isAnswered = false
            transitioningToNextQuestion = false
        }
    }

    val speechResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                answer = it[0]
            }
        }
    }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
    }

    val recordAudioPermissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            speechResultLauncher.launch(speechIntent)
        }
    }

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
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState is UiState.Success) {
                    fullText = (uiState as UiState.Success).outputText

                    val answerStartIndex = fullText.indexOfFirst { it == 'A' }

                    if (answerStartIndex != -1) {
                        questionText = fullText.substring(0, answerStartIndex).trim()
                        val answersText = fullText.substring(answerStartIndex).trim()
                        answersList = answersText.split(Regex("(?=\\s*[A-D]\\)\\s*)")).map { it.trim() }.filter { it.isNotEmpty() }
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
                        textAlign = TextAlign.Center
                    )
                    // Mostrar las opciones de respuesta
                    answersList.forEachIndexed { index, answerOption ->
                        Button(
                            onClick = {
                                // Manejar la respuesta aquí
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = answerOption)
                        }
                    }
                }
            }

            // Mostrar la explicación después de la espera
            if (hasAnswered && explanationShown) {
                Text(
                    text = fullText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Fila con el campo de texto y botones fijos en la parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Campo de texto y botones fijos en la parte inferior
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!explanationShown) {
                        TextField(
                            value = answer,
                            label = { Text("Write your response") },
                            onValueChange = { answer = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .heightIn(min = 56.dp)
                                .border(1.dp, Color.Gray),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            speechResultLauncher.launch(speechIntent)
                                        } else {
                                            recordAudioPermissionRequest.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Voice Input"
                                    )
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        val normalizedAnswer = answer.lowercase().trim()
                        if (normalizedAnswer in listOf("a", "b", "c", "d")) {
                            triviaViewModel.checkAnswer(normalizedAnswer)
                            showNextButton = true
                            answer = ""
                            hasAnswered = true
                            isAnswered = true
                        } else {
                            result = "Invalid answer. Please enter a, b, c, or d."
                        }
                    },
                    enabled = !isAnswered && answer.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(text = "Send response")
                }

                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

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
                // Agregar un Spacer para mover el texto más abajo
                Spacer(modifier = Modifier.height(12.dp)) // Ajusta la altura según sea necesario
                // Texto que indica la generación de las preguntas por AI Gemini
                Text(
                    text = "Las preguntas son generadas usando la AI Gemini.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


